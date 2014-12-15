FancyMessageFormat
==================

## Format Specifications
####Colors:
* Colors normally go on until changed/removed, but they reset on new lines
* Colors may only be defined within the text-line (no special json attribute)
* Color names (square brackets will be around these): http://jd.bukkit.org/rb/doxygen/d7/dc0/enumorg_1_1bukkit_1_1ChatColor.html#pub-attribs
* Colors in ‘hover’ tags are separate (start with nothing, resetting each line)
* Colors are not case sensitive

####Breaks:
* The break symbol is: [break]
* Will break to the next line in chat, so if [break][break] is used it will leave an empty line
* The end of a list element (string) does NOT automatically result in a break in the chat
* After using [break] there may not be more text in this line, from there only more [break] tags are allowed (more text can go in a new list entry)

####Tags:
* Tags do not need to be indented, but normally are for readability
* Tags are not case sensitive
* Supported tags:
  * Link: If clicked it will cause a “Do you want to visit this website?” popup
  * Hover: If hovered over with the mouse it will give information in a little box
  * Suggest: If clicked it will put a string into the chat box
  * Command: If clicked it will execute a command as the player
* The following equivalents and alternatives are supported:
  * [STRIKETHROUGH] = [s]
  * [BOLD] = [b]
  * [ITALIC] = [i]
  * [UNDERLINE] = [u]

####Escaping:
* [esc] can be used to escape 1 character, so if you want to have "link: example.com" as text you need a [esc] in front of this. Then the first character will be escaped and since "ink: " is not a valid tag anymore it will be shown as text instead of being used as link on the previous text part.
* Placing "[esc][esc]" in the text will result in the text "[esc]" being printed, the first escape will be handled as such, this will escape [ and therefore the text behind this will be "esc]" and this is just text
* Also works for variables

####Listing of all tags
Colors: white, black, yellow, gold, aqua, dark_aqua, blue, dark_blue, light_purple, 
dark_purple, red, dark_red, green, dark_green, gray, dark_gray
Other: bold, italic, underlined, strikethrough, obfuscated, plus their equivalent close-tags

JSON tags:
* hover:  Hover text. Can contain all formatting, but no [break] tags
* Link: pure text, formatting won't do anything
* Suggest: pure text, formatting won’t do anything
* Command: pure text, formatting won’t do anything
Other: [BREAK], [ESC]

## Format Guidelines
* Use lower/upper-case characters for tags

####TODO:
* Add examples
* Add more guidelines (spacing etc.)
