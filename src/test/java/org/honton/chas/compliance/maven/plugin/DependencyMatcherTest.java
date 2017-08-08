package org.honton.chas.compliance.maven.plugin;

import java.util.Arrays;
import java.util.Collections;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 */
public class DependencyMatcherTest {

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
  public void testNullGlobs() throws MojoExecutionException {
    Log log = Mockito.mock(Log.class);
    DependencyMatcher dependencyMatcher = new DependencyMatcher(log, Collections.<String>emptyList());
    Dependency dependency = createDependency("groupId", "artifactId");
    Assert.assertFalse(dependencyMatcher.isMatch(dependency));
  }

  @Test
  public void testEmptyGlobs() throws MojoExecutionException {
    Log log = Mockito.mock(Log.class);
    DependencyMatcher dependencyMatcher = new DependencyMatcher(log, null);
    Dependency dependency = createDependency("groupId", "artifactId");
    Assert.assertFalse(dependencyMatcher.isMatch(dependency));
  }

  @Test(expected = MojoExecutionException.class)
  public void testGlobWithNoColon() throws MojoExecutionException {
    Log log = Mockito.mock(Log.class);
    new DependencyMatcher(log, Collections.singletonList("groupId"));
  }

  @Test(expected = MojoExecutionException.class)
  public void testGlobWithFourColon() throws MojoExecutionException {
    Log log = Mockito.mock(Log.class);
    new DependencyMatcher(log, Collections.singletonList("g:a:c:t:x"));
  }

  @Test
  public void testGroupIdArtifactId() throws MojoExecutionException {
    Log log = Mockito.mock(Log.class);
    DependencyMatcher dependencyMatcher = new DependencyMatcher(log, Collections.singletonList("groupId:artifactId"));
    Dependency dependency = createDependency("groupId", "artifactId");
    Assert.assertTrue(dependencyMatcher.isMatch(dependency));
  }

  @Test
  public void testGroupIdArtifactIdClassifier() throws MojoExecutionException {
    Log log = Mockito.mock(Log.class);
    DependencyMatcher dependencyMatcher = new DependencyMatcher(log, Collections.singletonList("groupId:artifactId:classifier"));
    Dependency dependency = createDependency("groupId", "artifactId", "classifier");
    Assert.assertTrue(dependencyMatcher.isMatch(dependency));
  }

  @Test
  public void testGroupIdArtifactIdTypeClassifier() throws MojoExecutionException {
    Log log = Mockito.mock(Log.class);
    DependencyMatcher dependencyMatcher = new DependencyMatcher(log, Collections.singletonList("groupId:artifactId:type:classifier"));
    Dependency dependency = createDependency("groupId", "artifactId", "classifier", "type");
    Assert.assertTrue(dependencyMatcher.isMatch(dependency));
  }

  @Test
  public void testMatchSecond() throws MojoExecutionException {
    Log log = Mockito.mock(Log.class);
    DependencyMatcher dependencyMatcher = new DependencyMatcher(log, Arrays.asList("g:a", "groupId:artifactId"));
    Dependency dependency = createDependency("groupId", "artifactId");
    Assert.assertTrue(dependencyMatcher.isMatch(dependency));
  }

  @Test
  public void testNoMatch() throws MojoExecutionException {
    Log log = Mockito.mock(Log.class);
    DependencyMatcher dependencyMatcher = new DependencyMatcher(log, Collections.singletonList("groupId:artifactId"));
    Dependency dependency = createDependency("g", "a");
    Assert.assertFalse(dependencyMatcher.isMatch(dependency));
  }
}
