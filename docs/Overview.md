# Code Overview

The Drozer Agent is composed of 4 modules.

1. Agent - Main android app, has intents, activities, receivers etc...
2. JSolar - Formerly JDiesel, android library in charge of handling all protobuf communication between the Drozer python client and the Drozer Agent
3. tlslib - Direct copy of mwrtls, android library providing the trust stores as well as X509certificate finger/thumbprint calculations
4. androidlib - Android Library providing an easy wrapper for showing notifications

The main functionality of the agent is in the Agent and JSolar modules. and as such those will be documented here.

## Agent

see agent.md

## JSolar

see Jsolar.md


# Building

Building the project should be as simple as cloning the project via android studio, and then pressing the build button.
If you have any issues what so ever feel free to reach out to me on slack @William Ben Embarek

Next steps for the agent:
Code comments
Gitlab runners for building
Proper code linting
Cleaning of Ken's yayxyay methods and comments in the codebase