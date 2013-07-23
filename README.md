drozer
======

drozer (formerly Mercury) is the leading security testing framework for Android.

drozer allows you to search for security vulnerabilities in apps and devices by assuming the role of an app and interacting with the Dalvik VM, other apps' IPC endpoints and the underlying OS.

drozer provides tools to help you use, share and understand public Android exploits. It helps you to deploy a drozer Agent to a device through exploitation or social engineering. Using weasel (MWR's advanced exploitation payload) drozer is able to maximise the permissions available to it by installing a full agent, injecting a limited agent into a running process, or connecting a reverse shell to act as a Remote Access Tool (RAT).

drozer is open source software, maintained by MWR InfoSecurity, and can be downloaded from:

    mwr.to/drozer


Installing
----------

The drozer agent can be built in Eclipse or using the ant build system.

You need to make some other sources available for building:

  * $ <workspace>/agent => this directory
  * $ <workspace>/jdiesel => cloned from https://github.com/mwrlabs/jdiesel
  * $ <workspace>/mwr-android => cloned from https://github.com/mwrlabs/mwr-android
  * $ <workspace>/mwr-tls => cloned from https://github.com/mwrlabs/mwr-tls

You must also update the Git submodules in jdiesel:

  * `$ cd jdiesel`
  * `$ git submodule init && git submodule update`

Then, either import the projects into Eclipse and build, or run:

  * `$ cd agent`
  * `$ ant debug`

It is recommended to install the drozer agent using adb:

  * `$ adb install bin/agent.apk`


License
-------

drozer is released under a 3-clause BSD License. See LICENSE for full details.


Contacting the Project
----------------------

drozer is Open Source software, made great by contributions from the community.

For full source code, to report bugs, suggest features and contribute patches please see our Github project:

  https://github.com/mwrlabs/drozer

Bug reports, feature requests, comments and questions can be submitted sent to:

  drozer [at] mwrinfosecurity.com

Follow the latest drozer news, follow the project on Twitter:

  @mwrdrozer
