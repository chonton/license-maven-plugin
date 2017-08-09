package org.honton.chas.license.maven.plugin.compliance;

import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class LicenseSetTest {

  @Test
  public void loadLicenses() throws MojoExecutionException {
    List<LicenseRegex> licenses = LicenseSet.loadLicenses("osi");
    LicenseRegex apache = licenses.get(0);
    Assert.assertEquals("^(?-iu)(Apache License, Version 2\\.0)|(Apache-2\\.0)$", apache.getName());
    Assert.assertEquals("^https?://www\\.apache\\.org/licenses/LICENSE-2\\.0$", apache.getUrl());
  }

}
