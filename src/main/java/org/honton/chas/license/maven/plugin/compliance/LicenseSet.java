package org.honton.chas.license.maven.plugin.compliance;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
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

   public static LicenseSet loadLicenseSet(String resource) throws JAXBException, IOException {
    try (InputStream is = LicenseSet.class.getClassLoader().getResourceAsStream(resource + ".xml")) {
      JAXBContext jaxbContext = JAXBContext.newInstance(LicenseSet.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (LicenseSet) jaxbUnmarshaller.unmarshal(is);
    }
  }

  private static final Pattern COMMA_SEPARATED_LIST = Pattern.compile("\\s*,\\s*");

   public static List<LicenseRegex> loadLicenses(String resources) throws MojoExecutionException {
    List<LicenseRegex> licenses = new ArrayList<>();
    for(String resource : COMMA_SEPARATED_LIST.split(resources)) {
      try {
        licenses.addAll(loadLicenseSet(resource).licenses);
      } catch (JAXBException | IOException e) {
        throw new MojoExecutionException("Could not load licenses from " + resource, e);
      }
    }
    return licenses;
  }
}
