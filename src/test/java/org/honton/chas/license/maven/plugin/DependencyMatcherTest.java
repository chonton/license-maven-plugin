package org.honton.chas.license.maven.plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** */
class DependencyMatcherTest {

  static Dependency createDependency(String... gact) {
    Dependency d = new Dependency();
    switch (gact.length) {
      default:
        d.setType(gact[3]);
      case 3:
        d.setClassifier(gact[2]);
      case 2:
        d.setArtifactId(gact[1]);
      case 1:
        d.setGroupId(gact[0]);
      case 0:
    }
    return d;
  }

  @Test
  void testNullGlobs() throws MojoExecutionException {
    Log log = Mockito.mock(Log.class);
    DependencyMatcher dependencyMatcher =
        new DependencyMatcher(log, Collections.<String>emptyList());
    Dependency dependency = createDependency("groupId", "artifactId");
    Assertions.assertFalse(dependencyMatcher.isMatch(dependency));
  }

  @Test
  void testEmptyGlobs() throws MojoExecutionException {
    Log log = Mockito.mock(Log.class);
    DependencyMatcher dependencyMatcher = new DependencyMatcher(log, null);
    Dependency dependency = createDependency("groupId", "artifactId");
    Assertions.assertFalse(dependencyMatcher.isMatch(dependency));
  }

  @Test
  void testGlobWithNoColon() {
    Log log = Mockito.mock(Log.class);
    List<String> groupId = List.of("groupId");
    Assertions.assertThrows(
        MojoExecutionException.class, () -> new DependencyMatcher(log, groupId));
  }

  @Test
  void testGlobWithFourColon() {
    Log log = Mockito.mock(Log.class);
    List<String> globs = List.of("g:a:c:t:x");
    Assertions.assertThrows(MojoExecutionException.class, () -> new DependencyMatcher(log, globs));
  }

  @Test
  void testGroupIdArtifactId() throws MojoExecutionException {
    Log log = Mockito.mock(Log.class);
    DependencyMatcher dependencyMatcher =
        new DependencyMatcher(log, Collections.singletonList("groupId:artifactId"));
    Dependency dependency = createDependency("groupId", "artifactId");
    Assertions.assertTrue(dependencyMatcher.isMatch(dependency));
  }

  @Test
  void testGroupIdArtifactIdClassifier() throws MojoExecutionException {
    Log log = Mockito.mock(Log.class);
    DependencyMatcher dependencyMatcher =
        new DependencyMatcher(log, Collections.singletonList("groupId:artifactId:classifier"));
    Dependency dependency = createDependency("groupId", "artifactId", "classifier");
    Assertions.assertTrue(dependencyMatcher.isMatch(dependency));
  }

  @Test
  void testGroupIdArtifactIdTypeClassifier() throws MojoExecutionException {
    Log log = Mockito.mock(Log.class);
    DependencyMatcher dependencyMatcher =
        new DependencyMatcher(log, Collections.singletonList("groupId:artifactId:type:classifier"));
    Dependency dependency = createDependency("groupId", "artifactId", "classifier", "type");
    Assertions.assertTrue(dependencyMatcher.isMatch(dependency));
  }

  @Test
  void testMatchSecond() throws MojoExecutionException {
    Log log = Mockito.mock(Log.class);
    DependencyMatcher dependencyMatcher =
        new DependencyMatcher(log, Arrays.asList("g:a", "groupId:artifactId"));
    Dependency dependency = createDependency("groupId", "artifactId");
    Assertions.assertTrue(dependencyMatcher.isMatch(dependency));
  }

  @Test
  void testNoMatch() throws MojoExecutionException {
    Log log = Mockito.mock(Log.class);
    DependencyMatcher dependencyMatcher =
        new DependencyMatcher(log, Collections.singletonList("groupId:artifactId"));
    Dependency dependency = createDependency("g", "a");
    Assertions.assertFalse(dependencyMatcher.isMatch(dependency));
  }

  @Test
  void testWildPrefix() throws MojoExecutionException {
    Log log = Mockito.mock(Log.class);
    DependencyMatcher dependencyMatcher =
        new DependencyMatcher(log, Collections.singletonList("com\\.example\\.*:*"));
    Dependency dependency = createDependency("com.example.group", "artifact-lib");
    Assertions.assertTrue(dependencyMatcher.isMatch(dependency));
  }
}
