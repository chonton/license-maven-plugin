package org.honton.chas.compliance.maven.plugin.license;

import java.util.Arrays;
import java.util.Collections;
import org.apache.maven.model.License;
import org.apache.maven.plugin.logging.Log;
import org.honton.chas.compliance.maven.plugin.license.LicenseMatcher;
import org.honton.chas.compliance.maven.plugin.license.LicenseRegex;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 */
public class LicenseMatcherTest {

  @Test
  public void testNoLicense() {
    Log log = Mockito.mock(Log.class);
    LicenseMatcher licenseMatcher = new LicenseMatcher(log, Collections.<LicenseRegex>emptyList());
    Assert.assertFalse(licenseMatcher.hasAcceptableLicense(Collections.<License>emptyList()));
  }

  private static final String APACHE_NAME = "^(?-iu)(Apache License, Version 2\\.0)|(Apache-2\\.0)$";
  private static final String APACHE_URL =  "^https?://www\\.apache\\.org/licenses/LICENSE-2\\.0$";

  private static final String LGPL_NAME = "^(?-iu)(GNU Lesser General Public License, version 3)|(LGPL-3\\.0)$";
  private static final String LGPL_URL =  "^https?://www\\.gnu\\.org/licenses/lgpl-3\\.0(\\.[a-z]{2})?\\.html$";

  @Test
  public void testMatchName() {
    Log log = Mockito.mock(Log.class);
    LicenseMatcher licenseMatcher = new LicenseMatcher(log, Collections.singletonList(
      new LicenseRegex(APACHE_NAME, null)));

    License apacheName = new License();
    apacheName.setName("Apache License, Version 2.0");
    Assert.assertTrue(licenseMatcher.hasAcceptableLicense(Collections.singletonList(apacheName)));
    apacheName.setName("Apache-2.0");
    Assert.assertTrue(licenseMatcher.hasAcceptableLicense(Collections.singletonList(apacheName)));
  }

  @Test
  public void testMatchUrl() {
    Log log = Mockito.mock(Log.class);
    LicenseMatcher licenseMatcher = new LicenseMatcher(log, Collections.singletonList(
      new LicenseRegex(null, APACHE_URL)));

    License apacheUrl = new License();
    apacheUrl.setUrl("https://www.apache.org/licenses/LICENSE-2.0");
    Assert.assertTrue(licenseMatcher.hasAcceptableLicense(Collections.singletonList(apacheUrl)));
  }

  @Test
  public void testMatchSecondName() {
    Log log = Mockito.mock(Log.class);
    LicenseMatcher licenseMatcher = new LicenseMatcher(log, Arrays.asList(
      new LicenseRegex(APACHE_NAME, null), new LicenseRegex(LGPL_NAME, null)));

    License lgplName = new License();
    lgplName.setName("Apache License, Version 2.0");
    Assert.assertTrue(licenseMatcher.hasAcceptableLicense(Collections.singletonList(lgplName)));
    lgplName.setName("Apache-2.0");
    Assert.assertTrue(licenseMatcher.hasAcceptableLicense(Collections.singletonList(lgplName)));
  }

  @Test
  public void testMatchSecondUrl() {
    Log log = Mockito.mock(Log.class);
    LicenseMatcher licenseMatcher = new LicenseMatcher(log, Arrays.asList(
      new LicenseRegex(null, APACHE_URL), new LicenseRegex(null, LGPL_URL)));

    License lgplUrl = new License();
    lgplUrl.setUrl("https://www.gnu.org/licenses/lgpl-3.0.en.html");
    Assert.assertTrue(licenseMatcher.hasAcceptableLicense(Collections.singletonList(lgplUrl)));
  }

  @Test
  public void testNoMatch() {
    Log log = Mockito.mock(Log.class);
    LicenseMatcher licenseMatcher = new LicenseMatcher(log, Arrays.asList(
      new LicenseRegex(APACHE_NAME, APACHE_URL), new LicenseRegex(LGPL_NAME, LGPL_URL)));

    License fred = new License();
    fred.setName("fred's miserly terms");
    fred.setUrl("https://example.com/fred.html");
    Assert.assertFalse(licenseMatcher.hasAcceptableLicense(Collections.singletonList(fred)));
  }

}
