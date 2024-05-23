# license-maven-plugin

Check if maven dependencies meet license compliance requirements. 

# Rationale
Many organizations require checking open source dependencies' licenses to ensure that the dependency
use does not add legal risks to product development.  This due diligence helps prevent impairing the
organization's intellectual property.

# For more information
The [Open Source Initiative (OSI)](https://opensource.org/) reviews and categorizes open source
licences.  Additionally, OSI publishes a [FAQ](https://opensource.org/faq) on what is open source
and how it is licensed.

# Plugin
Plugin reports available at [plugin info](https://chonton.github.io/license-maven-plugin/plugin-info.html).

There is a single goal: [compliance](https://chonton.github.io/license-maven-plugin/compliance-mojo.html),
which binds by default to the *validate* phase.  This goal checks all dependencies in the build and
active profile sections for compliance with acceptable licenses.  

## Matching
For each project dependency, the goal checks if any of the dependency licenses match any of the
acceptable licenses.  A match is successful if either the dependency license URL matches the
acceptable license URL regular expression, or the dependency license name matches the acceptable
license name regular expression.  (The maven pom definition for [licenses](https://maven.apache.org/pom.html#Licenses)
recommends using the [spdx identifier](https://spdx.org/licenses/) as license name.)
Matches are case-insensitive and ignore leading and trailing whitespace.  The lack of a license in
a dependency will cause a match failure.

## Excluding Dependencies
The compliance check can be excluded for dependencies matching specified
`groupId:artifactId:type:classifier` glob patterns.  This can be used to turn off compliance check for
organizational internal dependencies that may not have an attached license.

## Configuration
| Parameter                            | Property                          | Default                          | Description                                                                                                                                                                                                         |
|--------------------------------------|-----------------------------------|----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| skipCompliance                       | ${compliance.skip}                | false                            | Skip the license check                                                                                                                                                                                              |
| acceptableLicenses                   |                                   |                                  | The set of license regular expressions to match against dependency licenses.  If any license is specified, the default acceptableLicenseResources will not be used.                                                 |
| acceptableLicenseResources           | ${compliance.licenses}            | osi-permissive                   | The comma separated names of xml resources from which to read licenses.  Built in resources are 'osi-widely-used' and 'osi-permissive'.  Default is used only if no acceptableLicenses are specified.               |
| acceptableLicenseResourcesFilesPaths | ${compliance.licenses.filesPaths} | /tmp/licenses.xml                | The comma separated file paths of xml resources from which to read licenses                                                                                                                                         |
| artifact                             | ${compliance.artifact}            | false                            | Check that main artifact has acceptable license                                                                                                                                                                     |
| excludes                             | ${compliance.excludes}            |                                  | The list of dependencies to exclude from checking compliance.  These will be in the form of *groupId:artifactId[[:type]:classifier]*. Wildcard characters '*' and '?' can be used to do glob-like pattern matching. |
| scopes                               | ${compliance.scopes}              | compile, runtime, provided, test | The comma separated list of scopes to check                                                                                                                                                                         |

# Examples

## Typical Use

Command line :
mvn org.honton.chas:license-maven-plugin:0.0.5:compliance "-Dcompliance.excludes=org.test:*,com.test:*" "-Dcompliance.licenses.filesPaths=/tmp/licences-custom.xml"

```xml
  <build>
    <pluginManagement>
        <plugins>
          <plugin>
            <groupId>org.honton.chas</groupId>
            <artifactId>license-maven-plugin</artifactId>
            <version>0.0.5</version>
          </plugin>
        </plugins>
    </pluginManagement>

    <plugins>
      <plugin>
        <groupId>org.honton.chas</groupId>
        <artifactId>license-maven-plugin</artifactId>
        <executions>
          <execution>
            <goals>
              <goal>compliance</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

## List licenses in pom
Specify that only Apache license is compliant:
```xml
    <configuration>
      <acceptableLicenses>
        <license>
          <name>(Apache License, Version 2\.0)|(Apache-2\.0)</name>
          <url>https?://www\.apache\.org/licenses/LICENSE-2\.0</url>
        </license>
      </acceptableLicenses>
    </configuration>
```

## Use both explicit acceptableLicenses and acceptableLicenseResources
```xml
    <configuration>
      <acceptableLicenses>
        <!-- Oracle Dual license -->
        <license>
          <name>(GPL2\s+w/\s+CPE)</name>
          <url>https?://glassfish\.java\.net/public/CDDL\+GPL_1_1\.html</url>
        </license>
      </acceptableLicenses>
      <acceptableLicenseResources>osi-permissive</acceptableLicenseResources>
    </configuration>
```
## Exclude Dependencies
Exclude checking specific artifact or artifacts from a group
```xml
    <configuration>
      <excludes>
        <!-- The POM for org.eclipse.microprofile.maven:microprofile-maven-build-extension
        is missing when resolving org.eclipse.microprofile.config:microprofile-config-api -->
        <exclude>org.eclipse.microprofile.config:microprofile-config-api</exclude>
        <!-- don't check for license of any artifact with matching group -->
        <exclude>org.honton.chas:*</exclude>
      </excludes>
    </configuration>
```

## Scope
Only check non-test scopes
```xml
    <configuration>
      <scopes>compile,runtime,provided</scopes>
    </configuration>
```

## Artifact
Check that main artifact's pom has an acceptable license defined in [licenses section](https://maven.apache.org/pom.html#Licenses)
```xml
    <configuration>
      <artifact>true</artifact>
    </configuration>
```
