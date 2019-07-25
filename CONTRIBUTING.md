# General Guideline
* Indents are four-spaces
* I highly recommend to follow the [Code Convention](http://www.oracle.com/technetwork/java/codeconvtoc-136057.html)

# Contribution
## Wiki
Everybody has different perspective, and it's hard for me to fill everything up while working on the code. More importantly,
writing down information doesn't necessarily will help people who read it. So if you know how to use TriggerReactor and want to make
Wiki even better, please don't wait for my permission. The Wiki section is open to public, and all you need to do is pressing edit button.

* Please use appropriate language (no slang, etc)
    * But of course, I don't care about what you put in the example. For example, `#MESSAGE "hue hue"`
* Use the information only based on fact.

## Executor/Placeholder (Javascript)
If you have created plugin yourself, and you think you want to contribute, Executors and Placeholders are good place. They are written
in Javascript, yet it doesn't require for you to know all the aspects of Javascript. I myself even don't know well about programming
in Javascript, but you really can find that almost all parts of Javascript are similar to Java.

To contribute, fork TriggerReactor and add/modify/remove Executors and Placeholders under 
[src/main/resources](https://github.com/wysohn/TriggerReactor/tree/master/src/main/resources). Once you are done, make pull request,
then I will add it to the main branch.

* You must test your code before making pull request. Testing Executor and Placeholder are easy as you can just paste your js file into
the appropriate folder, and use /trg reload command to load it without restarting the server.
* If tests are done, you must explain about your work to people.
    * If you created a new Executor/Placeholder, update the Wiki, so people will be able to know how to use what you've made
    * If you modified Executor/Placeholder, make sure to update the Wiki as necessary.

## Core code
If you know Java very well and are experienced plugin developer (and knowing data structure or algorithms is plus), you may contribute to fix bugs, add new features, etc. to the actual
TriggerReactor source code. Once you are done, make pull request.

* Please test your code yourself.
* Please update Wiki as necessary. 

## Pull Request
When you submit the code, first of all, thank you for your participation. But please do it to the `development` branch so that we can test the code before put it into the release version.

## Indentation
Please indent where it is necessary. Even for yourself, it helps.

For less confusion for the community, only **use 4 spaces for indentation**. 

## FINALLY
Even if you think your contribution is small compared to others, it's still a contribution. So please don't hesitate to put your name on the plugin.yml. You are **highly encouraged** by me to do so. I usually put them myself too, but you are always welcome to be part of it if you have done anything to contribute to make TR even better.
