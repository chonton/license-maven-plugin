# license-maven-plugin

Check if maven dependencies meet license compliance requirements. 

# Rationale
Many organizations require the checking dependencies' licenses to ensure that open source sofware
use does not add legal risks to product development.  This due diligence helps prevent impairing the
organization's intellectual property.

# For more information
The [Open Source Initiative (OSI)](https://opensource.org/) reviews and categorizes open source
licences.  Additionally, OSI publishes a [FAQ](https://opensource.org/faq) on what is open source
and how it is licensed.

# Plugin
Plugin reports available at [plugin info](https://chonton.github.io/license-maven-plugin/0.0.1/plugin-info.html).

There is a single goal: [compliance](https://chonton.github.io/license-maven-plugin/0.0.1/local-mojo.html),
which binds by default to the *validate* phase.  This goal checks all dependencies in the build and
active profile sections for compliance with acceptable licenses.  

## Matching
For each project dependency, the goal checks if any of the dependency licenses match any of the
acceptable licenses.  A match is successful if either the dependency license URL matches the
acceptable license URL regular expression, or the dependency license name matches the acceptable
license name regular expression.  Matches are case-insensitive and ignore leading and trailing
whitespace.  The lack of a license in the dependency will cause a match failure.

## Excluding Dependencies
The compliance check can be excluded for dependencies matching specified
`groupId:artifactId:type:classifier` glob patterns.  This can be used to turn off compliance check for
organizational internal dependencies that may not have an attached license.

## Configuration
| Parameter       | Property     | Default | Description          |
|-----------------|------------- | ------- |----------------------|
|skipCompliance|${compliance.skip}| false | Skip the license check |
|acceptableLicenses|            |  |The set of license regular expressions to match against dependency licenses. If not set, licenses from acceptableLicenseResource are used. |
|acceptableLicenseResources|${compliance.licenses}|osi-permissive|The comma separated names of xml resources from which to read licenses.  Built in resources are 'osi-widely-used' and 'osi-permissive' |
|excludes |      | | The list of dependencies to exclude from checking compliance.  These will be in the form of *groupId:artifactId[[:type]:classifier]*. Wildcard characters '*' and '?' can be used to do glob-like pattern matching. |
|scopes   |${compliance.scopes}|compile, runtime, provided, test|The comma separated list of scopes to check |

# Examples

## Typical Use
```xml
  <build>
    <pluginManagement>
        <plugins>
          <plugin>
            <groupId>org.honton.chas</groupId>
            <artifactId>license-maven-plugin</artifactId>
            <version>0.0.1</version>
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
      <plugin>
        <groupId>org.honton.chas</groupId>
        <artifactId>license-maven-plugin</artifactId>
        <executions>
          <execution>
            <goals>
              <goal>compliance</goal>
            </goals>
            <configuration>
              <acceptableLicenses>
                <license>
                  <name>(Apache License, Version 2\.0)|(Apache-2\.0)</name>
                  <url>https?://www\.apache\.org/licenses/LICENSE-2\.0</url>
                </license>
              </acceptableLicenses>
            </configuration>
          </execution>
        </executions>
      </plugin>
```

## Exclude Dependencies
Exclude checking artifacts from group `org.honton.chas`
```xml
      <plugin>
        <groupId>org.honton.chas</groupId>
        <artifactId>license-maven-plugin</artifactId>
        <executions>
          <execution>
            <goals>
              <goal>compliance</goal>
            </goals>
            <configuration>
              <excludes>
                <!-- don't check for license of any artifact with matching group -->
                <exclude>org.honton.chas:*</exclude>
              </excludes>
            </configuration>
          </execution>
        </executions>
      </plugin>
```
