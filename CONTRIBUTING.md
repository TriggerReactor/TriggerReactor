## Wiki
You don't need to be able to code plugins to contribute to TriggerReactor.  Keeping the Wiki up to date is just as essential as the plugin code itself.  
If you know how to use TriggerReactor and want to make the wiki even better, please don't wait for my permission. The Wiki section is open to public, and all you need to do is press edit button.
If you want to know where to start contributing, look at issues with the [wiki](https://github.com/wysohn/TriggerReactor/issues?q=is%3Aissue+is%3Aopen+label%3Awiki) tag.

* Please use appropriate language (no slang, etc)
* If you include example code, make sure it is correct and try to make the examples practical. (use an example that might appear in an actual server)
* Use factual information.  No opinions.

## Executor/Placeholder (Javascript)
If you have created plugin yourself, and you think you want to contribute, Executors and Placeholders are good place. They are written in Javascript, yet it doesn't require for you to know all the aspects of Javascript.  Just some basic knowledge.

To contribute, fork TriggerReactor and add/modify/remove Executors and Placeholders under 
[bukkit/src/main/resources](https://github.com/wysohn/TriggerReactor/tree/development/bukkit/src/main/resources) and [sponge/src/main/resources](https://github.com/wysohn/TriggerReactor/tree/development/sponge/src/main/resources). Once you are done, make pull request,
then I will add it to the main branch

* You must test your code before making pull request. Testing Executor and Placeholder are easy as you can just paste your js file into
the appropriate folder, and use /trg reload command to load it without restarting the server.
* If tests are done, you must explain about your work to people.
    * If you created a new Executor/Placeholder, update the Wiki, so people will be able to know how to use what you've made
    * If you modified Executor/Placeholder, make sure to update the Wiki as necessary.

## Core code
If you know Java very well and are experienced plugin developer (and knowing data structure or algorithms is plus), you may contribute to fix bugs, add new features, etc. to the actual
TriggerReactor source code. Once you are done, make a pull request.

* Indents are four-spaces
* I highly recommend to follow the [Code Convention](http://www.oracle.com/technetwork/java/codeconvtoc-136057.html)
* Please test your code yourself.
* If possible, create a unit test for your code.
* Please update Wiki as necessary. 

## Pull Request
When you create a pull request for your code, please make sure to do it to the `development` branch so that we can test the code before putting it into the release version.

## FINALLY
Even if you think your contribution is small compared to others, it's still a contribution. So please don't hesitate to put your name on the plugin.yml. You are **highly encouraged** by me to do so. I usually put them myself too, but you are always welcome to be part of it if you have done anything to contribute to make TR even better.
