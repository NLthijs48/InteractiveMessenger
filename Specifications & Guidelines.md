FancyMessageFormat
==================

## Format Specifications
#### Colors & Formatting:
* Colors and formatting normally go on until changed/removed, but they reset on new lines
* Color tags:
  * [white], [black], [yellow], [gold], [aqua], [dark_aqua], [blue], [dark_blue], [light_purple], [dark_purple], [red], [dark_red], [green], [dark_green], [gray], [dark_gray]
  * When you use a color it will override the last one
* Formatting tags:
  * [bold], [italic], [underlined], [strikethrough], [obfuscated]
  * When you use a formatting tag it will enable this formatting
  * When you use a close tag of a format it will disable that formatting
* The color and formatting inside a hover is seperate (start with nothing, resets each line)
* Tags are not case sensitive
* The following tags are equivalents:
  * [strikethrough] = [s]
  * [bold] = [b]
  * [italic] = [i]
  * [underline] = [u]

#### Breaks (newlines):
* The break symbol is: [break]
* Using it will cause the text to wrap to the next line in chat, so if [break][break] is used it will leave an empty line
* The end of a list element (string) does NOT automatically result in a break in the chat
* After using [break] there may not be more text in this line, from there only more [break] tags are allowed (more text can go in a new list entry)

#### Special effect tags:
* Tags are for adding special effects to a part of the text, used as the following: "  link: http://google.com"
* Supported tags:
  * Link: If clicked it will cause a “Do you want to visit this website?” popup
  * Hover: If hovered over with the mouse it will give information in a little box
    * You can use multiple lines with this tag to get multi-line hover popups
  * Suggest: If clicked it will put a string into the chat box
  * Command: If clicked it will execute a command as the player
* The hover tag supports all colors and formatting like with normal text
* Tags do not need to be indented, but normally are for readability
* Tags are not case sensitive

#### Escaping:
* [esc] can be used to escape 1 character, so if you want to have "link: example.com" as text you need a [esc] in front of this. Then the first character will be escaped and since "ink: " is not a valid tag anymore it will be shown as text instead of being used as link on the previous text part.
* Placing "[esc][esc]" in the text will result in the text "[esc]" being printed, the first escape will be handled as such, this will escape [ and therefore the text behind this will be "esc]" and this is just text
* Also works for variables

## Format Guidelines
* Use lower/upper-case characters for tags
* When using a special effect tag indent the line by 2 spaces (up for debate)

## TODO:
* Add examples
* Add more guidelines
