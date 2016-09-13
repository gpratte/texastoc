texastoc
========

texastoc is a Java/Spring web application for a home poker game. Presently it has hardcoded rules 
- the amount to buy into the game
- the points allocated to the top ten
- ...

Administration for the game is browser based and assumes that the administrator is using a browser on a computer.

The mobile web site is written to be viewed on a phone or tablet or computer and is written using JQuery mobile.

The mobile application supports the running the game with
- a Players tab for adding players to the game, marking their buy in, marking what place they come in, ... 
- a Clock tab for starting and pausing the clock (also send text messages to players that opt in when rounds change)
- a Money tab that show the amount collected and the payouts.
- a Seating tab to randomly seat players (which text the players where their seat is)

The mobile application also supports viewing past results with
- a Game tab that shows the last game (or pick from previous games of the season)
- a Season tab that shows the results of the last season and has a link to the top ten list
- a Quarterly Season tab that shows the results of the current quarter  
- a Hosts tab that shows who hosted and how many times and the average start time and shows who has been the banker and how many times

Instead of iterating on this project to add business rules to allow for defining different point systems, buy in, clock round, etc a new github project "housetourney" will be used. 

Visit https://www.texastoc.com and Continue as Guest to see some of the features in read only mode. Names and dollar amounts have been obfuscated (for obvious reasons).

# LICENSE
Code is licensed under a unlimited license. See the LICENSE file for details.

