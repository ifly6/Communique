# Communiqué #

## Summary ##
Communiqué is a free, open-source, and platform-independent client for NationStates's Telegram API. It was originally inspired by Auralia's work on the matter and thus, recognised the limitations of writing on a Windows-only platform when most headless servers are Linux boxes.

Since the general gist of this program is the same goal as Auralia's AutoTelegram, it will naturally be following many of the same protocols and functions.

Note that it is *your* responsibility to know how the telegram API works. Use of this program is agreement that you understand those limitations and requirements. It also agrees that you cannot claim losses, damages, or other negative effects from the author if action is taken against your NationStates account.

## Documentation ##

### System Requirements ###
* Java JRE 8 (https://java.com/en/download/)
* A [NationStates](http://www.nationstates.net) nation
* A NationStates [API client key](http://www.nationstates.net/pages/api.html#telegrams)
* A NationStates [telegram ID and secret key](http://www.nationstates.net/pages/api.html#telegrams)

### Readme ###
1. Acquire an API client key. You will need to contact the NationStates moderators with a GHR (Getting Help Request) for one. You will need to provide the region for whom the client key is made (there can only be one per region), the person to whom the key is responsible, the manner by which you will use it, and the purpose for which you requested it.

2. Acquire your secret key and telegram id. They are unique keys for each telegram. Type these keys into the boxes at the bottom.

3. Make sure that your telegram follows the rate limit by using the checkbox.

4. Go to the recipients tab and add in your nations. There are a number of tags supported in the program as well as a NOT tag.
  - Nations go individually on each line.
  - Regions go individually on each line with the following syntax 'region:[name]'
  - WA delegates and nations receive their own syntax as well: 'WA:delegates' and 'WA:members'
  - To say 'not', put in a '/' before the tag
  - For example, I can say: 'region:europe' and '/imperium_anglorum', which will send telegrams to everyone in Europe except Imperium Anglorum. Or, I could say 'region:europe' and '/WA:members', which would send telegrams to all residents of Europe who are not WA members. 
  - You cannot invert the tag and say 'region:europe' and 'not the nations who are not in the World Assembly', since there is no method for querying 'nations not in the WA'.

5. Click the 'SEND' button to send the telegrams. This program supports the use of multiple API keys by creating multiple threads. However, for that, you would need multiple API keys.

6. You can save all the keys and all your recipients to a file. The file (if your calls completed without any errors) will include your client, secret, and telegram keys; as well as the recipients of the file, and the people to which the file was already sent. Do not share this file. You can load these files from disc to restore a previous configuration (or, create them by hand and skip manual configuration).

### Technical Information ####
* This program reports its UserAgent to the NationStates API as follows:
	`NationStates JavaTelegram (maintained by Imperium Anglorum, used by <client key>)`

## Changelog ##
While there will be a version number, currently, there is no such number, since, for obvious reasons, we are still in alpha. Version numbers will start after the program is released.

1. Version 1 comes in three flavours, Communiqué (GUI), Morse (old CLI), and Marconi (new CLI).
  - Communiqué has support for the keys, variable flags, and the saving and loading of the program configuration. 
  - Morse does not do any of this. It only has support for a simple list of recipients without support for loading or saving of anything more than the client key. 
  - Marconi is a headless client. It will only accept valid files from Communiqué and then read those files and use all the data in them. It does not write these files (other than updating their sent lists). 
  - Note that Morse will likely be phased out in favour of just Communiqué and Marconi.
