package org.honton.chas.compliance.maven.plugin.license;

import java.util.List;
import org.apache.maven.model.License;
import org.apache.maven.plugin.logging.Log;
import org.honton.chas.compliance.maven.plugin.license.LicenseRegex;

/**
 * Check dependencies' licenses for compliance
 */
public class LicenseMatcher {

  private final Log logger;

  /**
   * The licenses that are allowed.
   */
  private List<LicenseRegex> acceptableLicenses;

  public LicenseMatcher(Log logger, List<LicenseRegex> acceptableLicenses) {
    this.logger = logger;
    this.acceptableLicenses = acceptableLicenses;
    for (LicenseRegex acceptableLicense : acceptableLicenses) {
      acceptableLicense.checkRegex();
    }
  }

  public boolean hasAcceptableLicense(List<License> licenses) {
    for (License license : licenses) {
      if( isAcceptable(license) ) {
        logger.debug(license.getName() + '/' + license.getUrl() + " is acceptable");
        return true;
      }
    }
    return false;
  }

  private boolean isAcceptable(License license) {
    for (LicenseRegex acceptableLicense : acceptableLicenses) {
      if(acceptableLicense.matches(license)) {
        return true;
      }
    }
    return false;
  }
}
