package org.honton.chas.license.maven.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;


/**
 * Does an artifact match a list of globs
 */
public class DependencyMatcher {

  private final Log logger;
  private final List<Pattern> patterns;

  /**
   * Create a matcher
   *
   * @param globs The list of glob patterns
   */
  public DependencyMatcher(Log logger, List<String> globs) throws MojoExecutionException {
    this.logger = logger;
    if (globs != null) {
      patterns = new ArrayList<>(globs.size());
      for (String glob : globs) {
        patterns.add(createPattern(glob));
      }
    } else {
      patterns = null;
    }
  }

  /**
   * Does the given artifact match one of the globs?
   *
   * @return true, if the artifact matches
   */
  public boolean isMatch(Dependency dependency) {
    if (patterns != null) {
      String s = gatc(dependency);
      for (Pattern pattern : patterns) {
        if (pattern.matcher(s).matches()) {
          logger.debug(dependency.getArtifactId() + " matches " + pattern.pattern());
          return true;
        }
      }
    }
    return false;
  }

  private static final Pattern GATC = Pattern.compile("^([^:]+):([^:]+)((:([^:]+))?:([^:]+))?$");
  // groupId:artifactId[[:type]:classifier]

  private static Pattern createPattern(String glob) throws MojoExecutionException {
    Matcher matcher = GATC.matcher(glob);
    if (!matcher.matches()) {
      throw new MojoExecutionException(glob + " does not match groupId:artifactId[[:type]:classifier]");
    }
    StringBuilder sb = new StringBuilder(128);
    globToRegex(sb, matcher.group(1));
    globToRegex(sb, matcher.group(2));
    globToRegex(sb, matcher.group(5));
    globToRegex(sb, matcher.group(6));

    return Pattern.compile(sb.append('$').toString());
  }

  static private void globToRegex(StringBuilder dst, String src) {
    if (dst.length() == 0) {
      dst.append('^');
    } else {
      dst.append(':');
    }
    if (src == null) {
      dst.append(".*");
    } else {
      for (int i = 0; i < src.length(); ++i) {
        char c = src.charAt(i);
        switch (c) {
          case '.':
            dst.append("\\.");
            break;
          case '?':
            dst.append('.');
            break;
          case '*':
            dst.append(".*");
            break;
          default:
            dst.append(c);
        }
      }
    }
  }

  private String gatc(Dependency dependency) {
    StringBuilder sb = new StringBuilder(128);
    sb.append(dependency.getGroupId())
      .append(':').append(dependency.getArtifactId())
      .append(':').append(dependency.getType())
      .append(':');

    String classifier = dependency.getClassifier();
    if (classifier != null && !classifier.isEmpty()) {
      sb.append(classifier);
    }
    return sb.toString();
  }
}
