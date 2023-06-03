package org.honton.chas.license.maven.plugin.compliance;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.maven.plugin.MojoExecutionException;

/** Wrapper class to load a set of licenses from an xml resource. */
@XmlRootElement(name = "licenses")
public class LicenseSet {

  @XmlElement(name = "license")
  private List<LicenseRegex> licenses;

  public static LicenseSet loadLicenseSet(String resource)
      throws JAXBException, IOException, MojoExecutionException {
    InputStream is = LicenseSet.class.getClassLoader().getResourceAsStream(resource + ".xml");
    if (is == null) {
      throw new MojoExecutionException("resource '" + resource + ".xml' not found");
    }
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(LicenseSet.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (LicenseSet) jaxbUnmarshaller.unmarshal(is);
    } finally {
      is.close();
    }
  }

  private static final Pattern COMMA_SEPARATED_LIST = Pattern.compile("\\s*,\\s*");

  public static void loadLicenses(List<LicenseRegex> licenses, String resources)
      throws MojoExecutionException {
    for (String resource : COMMA_SEPARATED_LIST.split(resources)) {
      try {
        licenses.addAll(loadLicenseSet(resource).licenses);
      } catch (JAXBException | IOException e) {
        throw new MojoExecutionException("Could not load licenses from " + resource, e);
      }
    }
  }
}
