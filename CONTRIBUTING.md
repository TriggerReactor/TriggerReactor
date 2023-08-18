## Wiki

You don't need to be able to code plugins to contribute to TriggerReactor. Keeping the Wiki up to date is just as
essential as the plugin code itself.

If you know how to use TriggerReactor and want to make the wiki even better, please don't wait for my permission. The
Wiki section is open to public, and all you need to do is press edit button.
If you want to know where to start contributing, look at issues with
the [wiki](https://github.com/wysohn/TriggerReactor/issues?q=is%3Aissue+is%3Aopen+label%3Awiki) tag.

* Please use appropriate language (no slang, etc)
* If you include example code, make sure it is correct and try to make the examples practical. (use an example that
  might appear in an actual server)
* Use factual information. No opinions.

## Branches

Open new branches freely, but keep in mind that these two branches have dedicated usage.

* `master` is for release.
* `development` is for beta.

Other than these two, you are free to add more branches as needed. More branch is always better in fact.

## Development Cycle

Please understand that we use Kanban to keep track of development.

* Check [this](https://github.com/wysohn/TriggerReactor/projects/) to see the task in progress, under testing, etc.

Using the Kanban board allow us to easily keep track of what we have worked on.

### Versioning

Assign version according to this rule

* For major updates, change the first digit. `2.x.x -> 3.0.0`
* For minor updates, change the second digit. `x.5.x -> x.6.0`
* For bug-fixes, change the last digit. `x.x.3 -> x.x.4`

However, since it's hard to keep track of bugs in different versions (as they can occur in any version),
use [Bugs](https://github.com/wysohn/TriggerReactor/projects/5) specifically for bug tracking.

### The Process

Please follow the general guideline below for smooth development process. It's essential for us to work as a single
team. You must look at [this](https://github.com/wysohn/TriggerReactor/projects/) to understand the process below.

##### 1. To do [Meet/Plan/Design]

First, before do any sort of work, propose the idea in the Issue Tracker. It can be simple as just a title indicating
what it has to do or as to very detailed description of what it will add to the TriggerReactor project.

For example, if you plan to add a Timings support

    Add Timings Support

    Timings will collect execution time from various Executors, Placeholders, and code interpretation
    as a form of hierarchical tree so that the users can see which triggers are draining the server
    resources. Similar idea to Spigot's /timings command.

    /trg timings toggle - toggle timings
    /trg timings print - print timings info
        ...

Then, the ideal scenario is meet up in person and talk face to face, yet it's not likely possible since everyone has
different schedule, different time zone, and have own life to live! So the best option is to `plan` it in the Issue
Tracker and ask for feedback from each other if possible. But if you are so certain that the plan will work out as
planned, just go ahead and start designing the proposed plan.

`design` **does not means code it right away**. Even though we can't predict a lot on early stage, at least we can draw
some big picture of how it would work. This significantly fasten the development cycle since coding is a hard labor, so
designing, using UML or whatever you feel comfortable, minimizes the wasting of time on coding and desining at the same
time. Also, if you are willing to, you can ask other teams to check on your design so that they may find something you
couln't see. (Suggestions: draw.io, visual paradigm, lucidchart)

When this stage is done, add it to the `To do` of the Kanban board.

##### 2. In Progress [Develop]

The task that we are currently working on. Make sure to assign yourself (and co-worker) in order to avoid different
people working on the same task.

This is where you code exactly as planned in the `Design` process. However, as I mentioned, the design will never be
perfect, so don't obsessed too much about following it exactly yet treat it as a general map to help you code.

When you start working on it, create a new branch instead of working on the development branch directly. By doing so,
you can use Pull Request feature of Github.

##### 3. Testing [Test/Evaluate]

The task that is implemented and need to be tested. This can be skipped if you have your unit test written already, or
if it's hard to test it via unit testing, test it manually. Just test that everything written in the task is working as
intended, not more or less.

When test is done, make a Pull Request with the new branch created earlier. In Pull Request, you may ask for review from
other teams, or if you think it's not necessary, merge it by

    Squash and Merge -> Create a Merge Commit

At this point, the process is done, so you may go back to 1. 2. or 3. to continue the development cycle.

##### 4. Beta

The task that is implemented and fully tested by developer, so it's ready for user-acceptance test. We release it
as `pre-release` so people can try the new features and provide feedbacks or report bugs.

##### 5. Done

The task that is ready to be released, and they will be added

Always add cards to the `To do`, and move the cards to left or right columns as needed. Our aim is to move all the cards
in the `To do` to `Done` in the end.

## Commit

Commit messages has to be as detail as possible, so anybody who sees it can have at least some idea of what you have
done in the commit.

* For the commit title, use the following rules. By doing so, Github can keep track of what issue the commit is related
  to.
*
    * Use the prefix `Fixes #X - ` if you fixed a bug. `Fixes #123 - This fixes that`
*
    * Use the prefix `Resolve #X - ` anything other than bug fixes. `Resolve #321 - Something new something`

Putting the prefix allow Github to automatically move the cards to `Done` when these commits are pushed into master
branch.

## Executor/Placeholder (Javascript)

If you have created plugin yourself, and you think you want to contribute, Executors and Placeholders are good place.
They are written in Javascript, yet it doesn't require for you to know all the aspects of Javascript. Just some basic
knowledge.

To contribute, fork TriggerReactor and add/modify/remove Executors and Placeholders under
[bukkit/src/main/resources](https://github.com/wysohn/TriggerReactor/tree/development/bukkit/src/main/resources)
and [sponge/src/main/resources](https://github.com/wysohn/TriggerReactor/tree/development/sponge/src/main/resources).
Once you are done, make pull request,
then I will add it to the main branch

* You must test your code before making pull request. Testing Executor and Placeholder are easy as you can just paste
  your js file into
  the appropriate folder, and use /trg reload command to load it without restarting the server.
* If tests are done, you must explain about your work to people.
    * If you created a new Executor/Placeholder, update the Wiki, so people will be able to know how to use what you've
      made
    * If you modified Executor/Placeholder, make sure to update the Wiki as necessary.

## Core code

If you know Java very well and are experienced plugin developer (and knowing data structure or algorithms is plus), you
may contribute to fix bugs, add new features, etc. to the actual
TriggerReactor source code. Once you are done, make a pull request.

* Indents are four-spaces
* I highly recommend to follow the [Code Convention](http://www.oracle.com/technetwork/java/codeconvtoc-136057.html)
* Please test your code yourself.
* If possible, create a unit test for your code.
* Please update Wiki as necessary.

## Pull Request

When you create a pull request for your code, please make sure to do it to the `development` branch so that we can test
the code before putting it into the release version.

## FINALLY

Even if you think your contribution is small compared to others, it's still a contribution. So please don't hesitate to
put your name on the plugin.yml. You are **highly encouraged** by me to do so. I usually put them myself too, but you
are always welcome to be part of it if you have done anything to contribute to make TR even better.
