package org.honton.chas.license.maven.plugin.compliance;

import java.util.regex.Pattern;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.maven.model.License;

/**
 * License matcher
 */
public class LicenseRegex {

  @XmlElement
  private String name;
  @XmlElement
  private String url;

  @XmlTransient
  private Pattern namePattern;
  @XmlTransient
  private Pattern urlPattern;

  public LicenseRegex() {
  }

  public LicenseRegex(String name, String url) {
    this.name = name;
    this.url = url;
    checkRegex();
  }

  @XmlTransient
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @XmlTransient
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void checkRegex() {
    if (name != null) {
      namePattern = Pattern.compile(name);
    }
    if (url != null) {
      urlPattern = Pattern.compile(url);
    }
  }

  public boolean matches(License license) {
    return urlPattern != null && license.getUrl() != null && urlPattern.matcher(license.getUrl()).matches()
        || namePattern != null && license.getName() != null && namePattern.matcher(license.getName()).matches();
  }
}
