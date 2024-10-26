package org.honton.chas.license.maven.plugin;

import java.util.Arrays;
import java.util.regex.Pattern;

/** Does an artifact match a list of globs */
public class ScopeMatcher {

  private static final Pattern COMMA_SEPARATED_LIST = Pattern.compile("\\s*,\\s*");

  private final String[] scopes;

  /**
   * Create a matcher
   *
   * @param scopes The comma separated list of scopes
   */
  public ScopeMatcher(String scopes) {
    this.scopes = scopes != null ? sortedScopes(scopes) : null;
  }

  private static String[] sortedScopes(String scopeSpec) {
    String[] scopes = COMMA_SEPARATED_LIST.split(scopeSpec);
    Arrays.sort(scopes);
    return scopes;
  }

  /**
   * Does the given artifact match one of desired scopes?
   *
   * @return true, if the artifact matches
   */
  public boolean isMatch(String scope) {
    return scopes == null
        || scope == null // some artifacts are pushed to maven central without scope
        || 0 <= Arrays.binarySearch(scopes, scope);
  }
}
