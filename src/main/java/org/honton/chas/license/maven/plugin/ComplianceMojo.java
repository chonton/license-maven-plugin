package org.honton.chas.license.maven.plugin;

import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.honton.chas.license.maven.plugin.compliance.LicenseMatcher;
import org.honton.chas.license.maven.plugin.compliance.LicenseRegex;
import org.honton.chas.license.maven.plugin.compliance.LicenseSet;

/**
 * This compliance goal checks all dependencies in the build and active profile sections for
 * compliance with acceptable licenses. For each dependency, this goal checks if any of the
 * dependency licenses matches any of the acceptable licenses. A match is successful if either the
 * dependency license URL matches the acceptable license URL regular expression, or the dependency
 * license name matches the acceptable license name regular expression. The lack of a license in the
 * dependency will cause this goal to fail.
 */
@Mojo(name = "compliance", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class ComplianceMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  @Parameter(defaultValue = "${session}", required = true, readonly = true)
  private MavenSession session;

  @Component private RepositorySystem repository;

  @Component private ProjectBuilder projectBuilder;

  @Component private ArtifactResolver artifactResolver;

  /** Skip checking licence compliance */
  @Parameter(property = "compliance.skip", defaultValue = "false")
  private boolean skipCompliance;

  /**
   * The licenses that are allowed. Each license has a name and URL regular expression. If not set,
   * licenses from acceptableLicenseResource are used.
   */
  @Parameter private List<LicenseRegex> acceptableLicenses;

  /** The resource containing licenses that are allowed. */
  @Parameter(property = "compliance.licenses")
  private String acceptableLicenseResources;

  /** Paths to files containing resources with licenses that are allowed */
  @Parameter(property = "compliance.licenses.filesPaths")
  private List<String> acceptableLicenseResourcesFilesPaths;

  /**
   * The dependencies to exclude from checking compliance. These will be in the form of
   * <em></em>groupId:artifactId[[:type]:classifier]</em>. Wildcard characters '*' and '?' can be
   * used to do glob-like pattern matching.
   */
  @Parameter(property = "compliance.excludes")
  private List<String> excludes;

  /** The dependency scopes to check. */
  @Parameter(property = "compliance.scopes", defaultValue = "compile, runtime, provided, test")
  private String scopes;

  /** Check main artifact */
  @Parameter(property = "compliance.artifact", defaultValue = "false")
  private boolean artifact;

  private DependencyMatcher excludeMatcher;
  private LicenseMatcher licenseMatcher;
  private ScopeMatcher scopeMatcher;

  // groupId:artifactId:packaging:classifier:version
  private static StringBuilder createMessage(StringBuilder sb, Dependency d) {
    sb.append(d.getGroupId())
        .append(':')
        .append(d.getArtifactId())
        .append(':')
        .append(d.getType() != null ? d.getType() : "jar");
    if (d.getClassifier() != null) {
      sb.append(':').append(d.getClassifier());
    }
    sb.append(':').append(d.getVersion());
    return sb;
  }

  private static String createMessage(Dependency d, String suffix) {
    return createMessage(new StringBuilder(100), d).append(suffix).toString();
  }

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skipCompliance) {
      getLog().info("skipping license compliance");
      return;
    }

    scopeMatcher = new ScopeMatcher(scopes != null ? scopes : "compile, runtime, provided, test");

    if (acceptableLicenses == null) {
      acceptableLicenses = new ArrayList<>();
      if (acceptableLicenseResources == null) {
        acceptableLicenseResources = "osi-permissive";
      }
    }
    if (acceptableLicenseResources != null) {
      LicenseSet.loadLicenses(acceptableLicenses, acceptableLicenseResources);
    }
    if (acceptableLicenseResourcesFilesPaths != null) {
      LicenseSet.loadLicensesFromFile(acceptableLicenses, acceptableLicenseResourcesFilesPaths);
    }

    excludeMatcher = new DependencyMatcher(getLog(), excludes);
    licenseMatcher = new LicenseMatcher(getLog(), acceptableLicenses);

    for (Dependency dependency : project.getDependencies()) {
      checkDependency(dependency);
    }

    if (artifact) {
      Dependency dependency = new Dependency();
      dependency.setGroupId(project.getGroupId());
      dependency.setArtifactId(project.getArtifactId());
      dependency.setVersion(project.getVersion());
      dependency.setScope("compile");
      dependency.setType(project.getPackaging());
      checkProject(dependency, project);
    }
  }

  private void checkDependency(Dependency dependency) throws MojoExecutionException {
    if (!scopeMatcher.isMatch(dependency.getScope())) {
      getLog().debug(createMessage(dependency, " is not scoped"));
      return;
    }
    if (excludeMatcher.isMatch(dependency)) {
      getLog().debug(createMessage(dependency, " is excluded"));
      return;
    }
    checkProject(dependency, getMavenProject(dependency));
  }

  private void checkProject(Dependency dependency, MavenProject mavenProject)
      throws MojoExecutionException {
    List<License> licenses = mavenProject.getLicenses();
    if (!licenseMatcher.hasAcceptableLicense(licenses)) {
      StringBuilder sb = createMessage(new StringBuilder(300), dependency);
      expandLicenses(mavenProject, sb);
      throw new MojoExecutionException(sb.toString());
    }
  }

  private void expandLicenses(MavenProject mavenProject, StringBuilder sb) {
    sb.append(" does not have acceptable license [");
    boolean priorLicense = false;
    for (License license : mavenProject.getLicenses()) {
      if (priorLicense) {
        sb.append("},{");
      } else {
        sb.append('{');
        priorLicense = true;
      }
      if (license.getName() != null) {
        sb.append("name:").append(license.getName());
      }
      if (license.getUrl() != null) {
        if (license.getName() != null) {
          sb.append(',');
        }
        sb.append("url:").append(license.getUrl());
      }
    }
    if (priorLicense) {
      sb.append('}');
    }
    sb.append(']');
  }

  private MavenProject getMavenProject(Dependency d) throws MojoExecutionException {
    Artifact artifact =
        repository.createProjectArtifact(d.getGroupId(), d.getArtifactId(), d.getVersion());
    try {
      return projectBuilder.build(artifact, session.getProjectBuildingRequest()).getProject();
    } catch (ProjectBuildingException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }
}
