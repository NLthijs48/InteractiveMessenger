InteractiveMessenger
====================

A format for text in config files to make use of the fancy Minecraft chat features like click, hover, suggest, etc.

## Information
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
        <version>1.1-SNAPSHOT</version>
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
1. Setup a language:
    1. Create a `languages` folder in `/src/main/resources` of your project.
    1. Create the first language file in the `languages` folder, for example `EN.yml`.
    1. Add a message to the language file:
    
        ```java
        message-key: "Hello %0%!"
        ```
1. Setup the LanguageManager:
    
    At the startup of your plugin (in `onEnable`) create a LanguageManager instance:
        
    ```java
    LanguageManager languageManager = new LanguageManager(
        this,                                  // The plugin (used to get the languages bundled in the jar file)
        "languages",                           // Folder where the languages are stored
        getConfig().getString("language"),     // The language to use indicated by the plugin user
        "EN",                                  // The default language, expected to be shipped with the plugin and should be complete, fills in gaps in the user-selected language
        Collections.singletonList("[MyPlugin] ") // Chat prefix to use with Message#prefix(), could of course come from the config file
    );
    ```
1. Send a message:
    
    ```java
    Message.fromKey("message-key").replacements(player.getName()).send(player);
    ```
1. What happens when starting the plugin?
    1. The LanguageManager will write all language files to the `languages` folder in the directory of your plugin (this keeps included languages up-to-date, while allowing the user to copy and edit the languages).
    1. The language selected by the user and default language file are loaded.
    1. At `Message.fromKey()` the message will get loaded from the user-selected language file (falling back to the default language file).
    1. `Message.replacements()` indicates that `%0%` should get replaced by the users name.
    1. `Message.send()` executes the replacements and sends the message to the Player/CommandSender/Logger.

## Example
The plugin [AreaShop](https://github.com/NLthijs48/AreaShop) is using this library and can be checked for advanced usage of this library. The following parts of AreaShop use this library:

* **[AreaShop](https://github.com/NLthijs48/AreaShop/blob/master/AreaShop/src/main/java/me/wiefferink/areashop/AreaShop.java).setupLanguageManager()**: setup the library
* **[AreaShop](https://github.com/NLthijs48/AreaShop/blob/master/AreaShop/src/main/java/me/wiefferink/areashop/AreaShop.java).message** method used to send messages, could also be done by using `Message.from()` directly.
* **[/lang](https://github.com/NLthijs48/AreaShop/tree/master/AreaShop/src/main/resources/lang)**: folder with the language files.

