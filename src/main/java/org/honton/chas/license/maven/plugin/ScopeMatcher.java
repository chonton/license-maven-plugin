package org.honton.chas.license.maven.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;


/**
 * Does an artifact match a list of globs
 */
public class ScopeMatcher {

  private final Log logger;

  private static final Pattern COMMA_SEPARATED_LIST = Pattern.compile("\\s*,\\s*");

  private final String[] scopes;

  /**
   * Create a matcher
   *
   * @param scopes The comma separated list of scopes
   */
  public ScopeMatcher(Log logger, String scopes) throws MojoExecutionException {
    this.logger = logger;
    this.scopes = COMMA_SEPARATED_LIST.split(scopes);
    Arrays.sort(this.scopes);
  }

  /**
   * Does the given artifact match one of the globs?
   *
   * @return true, if the artifact matches
   */
  public boolean isMatch(String scope) {
    return 0<=Arrays.binarySearch(scopes, scope);
  }
}
