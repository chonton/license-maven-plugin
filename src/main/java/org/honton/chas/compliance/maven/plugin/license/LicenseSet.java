package org.honton.chas.compliance.maven.plugin.license;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Wrapper class to load a set of licenses from an xml resource.
 */
@XmlRootElement(name = "licenses")
public class LicenseSet {

  @XmlElement(name = "license")
  private List<LicenseRegex> licenses;

  static public LicenseSet loadLicenseSet(String resource) throws JAXBException, IOException {
    try (InputStream is = LicenseSet.class.getClassLoader().getResourceAsStream(resource + ".xml")) {
      JAXBContext jaxbContext = JAXBContext.newInstance(LicenseSet.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (LicenseSet) jaxbUnmarshaller.unmarshal(is);
    }
  }

  static public List<LicenseRegex> loadLicenses(String resource) throws MojoExecutionException {
    try {
      return loadLicenseSet(resource).licenses;
    } catch (JAXBException  | IOException e) {
      throw new MojoExecutionException("Could not load licenses from " + resource, e);
    }
  }
}
