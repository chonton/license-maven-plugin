package org.honton.chas.compliance.maven.plugin.license;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class LicenseSetTest {

  @Test
  public void loadLicenses() throws Exception {
    List<LicenseRegex> licenses = LicenseSet.loadLicenses("osi");
    Assert.assertEquals(1, licenses.size());
  }

}
