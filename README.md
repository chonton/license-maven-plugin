# license-maven-plugin

Check if maven dependencies meet license compliance.  Many organizations require the licenses of
dependencies meet certain conditions to allow the use of that open source software.  This due
diligence helps prevent unwanted impairment of organizational intellectual property.

Mojo details at [plugin info](https://chonton.github.io/license-maven-plugin/0.0.1/plugin-info.html)

There is a single goal: [compliance](https://chonton.github.io/license-maven-plugin/0.0.1/local-mojo.html),
which defaults to the *validate* phase.  This goal checks all dependencies in the build and active
profile sections for compliance with acceptable licenses.  For each dependency, the goal checks if
any of the dependency licenses match any of the acceptable licenses.  A match is successful if
either the dependency license URL matches the acceptable license URL regular expression, or the
dependency license name matches the acceptable license name regular expression.  The lack of a
license in the dependency will cause a goal failure.


| Parameter       | Default           | Description          |
|-----------------|------------------ |----------------------|
|skipCompliance   | ${compliance.skip} | Skip the license check |
|acceptableLicenses |             |The set of license regular expressions to match against dependency licenses. If not set, licenses from acceptableLicenseResource are used. |
|acceptableLicenseResources | osi |The name of an xml resource from which to read a set of licenses.  Built in resources are 'osi', 'osi-viral', 'osi-non-viral' |
|excludeDependencies |      |The dependencies to exclude from checking compliance.  These will be in the form of *groupId:artifactId[[:type]:classifier]*. Wildcard characters '*' and '?' can be used to do glob-like pattern matching. |

Typical use:

```xml
  <build>
    <plugins>

      <plugin>
        <groupId>org.honton.chas</groupId>
        <artifactId>license-maven-plugin</artifactId>
        <version>0.0.1</version>
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

Specifying that only Apache license is allowed:

```xml
  <build>
    <plugins>

      <plugin>
        <groupId>org.honton.chas</groupId>
        <artifactId>license-maven-plugin</artifactId>
        <version>0.0.1</version>
        <executions>
          <execution>
            <goals>
              <goal>compliance</goal>
            </goals>
            <configuration>
              <acceptableLicenses>
                <license>
                  <name>^(?-iu)(Apache License, Version 2\.0)|(Apache-2\.0)$</name>
                  <url>^https?://www\.apache\.org/licenses/LICENSE-2\.0$</url>
                </license>
              </acceptableLicenses>
            </configuration>
          </execution>
        </executions>
      </plugin>

    </plugins>
  </build>
```
