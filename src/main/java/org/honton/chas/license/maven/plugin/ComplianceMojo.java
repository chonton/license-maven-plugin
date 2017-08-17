package org.honton.chas.license.maven.plugin;

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
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.artifact.resolve.ArtifactResolver;
import org.honton.chas.license.maven.plugin.compliance.LicenseMatcher;
import org.honton.chas.license.maven.plugin.compliance.LicenseRegex;
import org.honton.chas.license.maven.plugin.compliance.LicenseSet;

import java.util.List;
import java.util.regex.Pattern;

/**
 * This compliance goal checks all dependencies in the build and active profile sections for
 * compliance with acceptable licenses.  For each dependency, this goal checks if any of the
 * dependency licenses matches any of the acceptable licenses.  A match is successful if either the
 * dependency license URL matches the acceptable license URL regular expression, or the dependency
 * license name matches the acceptable license name regular expression. The lack of a
 * license in the dependency will cause this goal to fail.
 */
@Mojo(name = "compliance", defaultPhase = LifecyclePhase.VALIDATE)
public class ComplianceMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  @Parameter(defaultValue = "${session}", required = true, readonly = true)
  private MavenSession session;

  @Component
  private RepositorySystem repositorySystem;

  @Component
  private ProjectBuilder mavenProjectBuilder;

  @Component
  private ArtifactResolver artifactResolver;

  /**
   * Skip checking licence compliance
   */
  @Parameter(property = "compliance.skip", defaultValue = "false")
  private boolean skipCompliance;

  /**
   * The licenses that are allowed.  Each license has a name and URL regular expression.
   * If not set, licenses from acceptableLicenseResource are used.
   */
  @Parameter
  private List<LicenseRegex> acceptableLicenses;

  /**
   * The resource containing licenses that are allowed.
   */
  @Parameter(property = "compliance.licenses", defaultValue = "osi-widely-used")
  private String acceptableLicenseResources;

  /**
   * The dependencies to exclude from checking compliance.  These will be in the form of
   * <em></em>groupId:artifactId[[:type]:classifier]</em>. Wildcard characters '*' and '?' can be
   * used to do glob-like pattern matching.
   */
  @Parameter
  private List<String> excludes;

  /**
   * The dependency scopes to check.
   */
  @Parameter(property = "compliance.scopes", defaultValue = "compile, runtime, provided, test")
  private String scopes;

  private DependencyMatcher excludeMatcher;
  private LicenseMatcher licenseMatcher;
  private ScopeMatcher scopeMatcher;

  private static final Pattern COMMA_SEPARATED_LIST = Pattern.compile("\\s*,\\s*");

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skipCompliance) {
      getLog().info("skipping license compliance");
      return;
    }

    scopeMatcher = new ScopeMatcher(getLog(), scopes != null ?scopes :"compile, runtime, provided, test");

    if( acceptableLicenses == null) {
      getLog().debug("using licenses from " + acceptableLicenseResources);
      acceptableLicenses = LicenseSet.loadLicenses(acceptableLicenseResources);
    }
    excludeMatcher = new DependencyMatcher(getLog(), excludes);
    licenseMatcher = new LicenseMatcher(getLog(), acceptableLicenses);

    for (Dependency dependency : project.getDependencies()) {
      checkDependency(dependency);
    }
  }

  private void checkDependency(Dependency dependency) throws MojoExecutionException, MojoFailureException {
    if(!scopeMatcher.isMatch(dependency.getScope())) {
      getLog().debug(createMessage(dependency, " is not scoped"));
      return;
    }
    if (excludeMatcher.isMatch(dependency)) {
      getLog().debug(createMessage(dependency, " is excluded"));
      return;
    }
    MavenProject mavenProject = getMavenProject(dependency);
    if (!licenseMatcher.hasAcceptableLicense(mavenProject.getLicenses())) {
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
    if(priorLicense) {
      sb.append('}');
    }
    sb.append(']');
  }

  private MavenProject getMavenProject(Dependency d) throws MojoFailureException {
    try {
      Artifact pomArtifact = repositorySystem.createProjectArtifact(d.getGroupId(), d.getArtifactId(), d.getVersion());
      ProjectBuildingResult build = mavenProjectBuilder.build(pomArtifact, session.getProjectBuildingRequest());
      return build.getProject();
    } catch (ProjectBuildingException e) {
      throw new MojoFailureException(createMessage("Could not build effective pom for ", d), e);
    }
  }

  // groupId:artifactId:packaging:classifier:version
  private static StringBuilder createMessage(StringBuilder sb, Dependency d) {
    sb.append(d.getGroupId())
        .append(':').append(d.getArtifactId())
        .append(':').append(d.getType()!=null ? d.getType() : "jar");
    if(d.getClassifier()!=null) {
      sb.append(':').append(d.getClassifier());
    }
    sb.append(':').append(d.getVersion());
    return sb;
  }

  private static String createMessage(Dependency d, String suffix) {
    return createMessage(new StringBuilder(100), d).append(suffix).toString();
  }

  private static String createMessage(String prefix, Dependency d) {
    return createMessage(new StringBuilder(100).append(prefix), d).toString();
  }
}
