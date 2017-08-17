package org.honton.chas.license.maven.plugin.compliance;

import org.apache.maven.model.License;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 *
 */
public class LicenseSetTest {

  @Test
  public void loadLicenses() throws MojoExecutionException {
    List<LicenseRegex> licenses = LicenseSet.loadLicenses("osi-widely-used");
    LicenseRegex apache = licenses.get(0);
    Assert.assertEquals("^(?-iu)\\s*((The\\s+)?Apache\\s+(Software\\s+)?License\\s*(,\\s*Version\\s+2\\.0)?)|(Apache-2\\.0)\\s*$", apache.getName());
    Assert.assertEquals("^https?://www\\.apache\\.org/licenses/LICENSE-2\\.0.*$", apache.getUrl());
  }

  private static License createLicense(String name, String url) {
    License license = new License();
    license.setName(name);
    license.setUrl(url);
    return license;
  }

  private static LicenseRegex loadLicenseRegex(String resources, int index) throws MojoExecutionException {
    List<LicenseRegex> licenses = LicenseSet.loadLicenses(resources);
    LicenseRegex apache = licenses.get(index);
    apache.checkRegex();
    return apache;
  }

  @Test
  public void testCompareApache() throws MojoExecutionException {
    License license = createLicense("Apache Software Licenses", "http://www.apache.org/licenses/LICENSE-2.0.txt");
    Assert.assertTrue(loadLicenseRegex("osi-non-viral", 0).matches(license));
  }


  @Test
  public void testCompareLgpl() throws MojoExecutionException {
    License license = createLicense("GNU Lesser Public License", null);
    Assert.assertTrue(loadLicenseRegex("osi-non-viral", 5).matches(license));
  }

}
