InteractiveMessaging
====================

A format for text in config files to make use of the fancy Minecraft chat features like click, hover, suggest, etc.

## Information
* **Build server:** http://wiefferink.me:8080/job/InteractiveMessenger/
* **Javadocs:** https://wiefferink.me/InteractiveMessenger/javadocs

## Authors
* **Thijs aka NLThijs48:** Initial code structure, refactoring and getting it production ready
* **Tobias aka Phoenix:** Initial code structure and initial parser

## Requirements
* Support for click effects, when a player clicks on text it will execute something
  * Execute a command (will always be executed by the player itself)
  * Open a link
  * Suggest a command (put certain text in the chat line)
* Support for hover effects, when a player hovers text it will display some info
  * Multiple lines for hover text
* Support for colors and formatting
* Support for multiple lines in a single message (go to the next line)

## Other targets
* Easy to read format, you can see what does what
  * Format allows to read the whole message text ignoring attributes (hover etc)
* Not too much overhead for defining the message
  * Problem with BBCode
