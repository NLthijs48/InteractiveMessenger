InteractiveMessenger
====================

A format for text in config files to make use of the fancy Minecraft chat features like click, hover, suggest, etc.

## Information
* **Build server:** http://jenkins.wiefferink.me/job/InteractiveMessenger/
* **Javadocs:** https://wiefferink.me/InteractiveMessenger/javadocs

## Authors
* **Thijs aka NLThijs48:** Initial code structure, refactoring and getting it production ready
* **Tobias aka Phoenix:** Initial code structure and initial parser

## Usage
1. Add Maven repository:

    ```xml
    <repositories>
      <repository>
        <id>nlthijs48</id>
        <url>http://maven.wiefferink.me</url>
      </repository>
    </repositories>
    ```
1. Add Maven dependency:

    ```xml
    <dependencies>
      <dependency>
        <groupId>me.wiefferink</groupId>
        <artifactId>interactivemessenger</artifactId>
        <version>1.0</version>
      </dependency>
    </dependencies>
    ```
1. Relocate the library (compatibility with other plugins):

    ```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>2.4.3</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <relocations>
                                <!-- Relocate InteractiveMessenger -->
                                <relocation>
                                    <pattern>me.wiefferink.interactivemessenger</pattern>
                                    <shadedPattern>your.package.here.shaded.interactivemessenger</shadedPattern>
                                </relocation>
                            </relocations>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
    ```
1. Use the library classes depending on how much of the InteractiveMessenger you want to use.
  * TODO: explain possible levels of integration and how to do them
  * TODO: add image showing integration levels
  
## Example
The plugin [AreaShop](https://github.com/NLthijs48/AreaShop) is using this library and can be checked for advanced usage of this library. The following parts of AreaShop use this library:

* **[AreaShop](https://github.com/NLthijs48/AreaShop/blob/master/AreaShop/src/main/java/me/wiefferink/areashop/AreaShop.java).setupLanguageManager()**: setup the library
* **AreaShop.message** method used to send messages, could also be done by using `Message.from()` directly though.
* **/lang**: folder with the language files.

