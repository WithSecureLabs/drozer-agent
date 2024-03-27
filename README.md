drozer
======

drozer (formerly Mercury) is a leading security testing framework for Android.

drozer allows you to search for security vulnerabilities in apps and devices by assuming the role of an app and interacting with the Dalvik VM, other apps' IPC endpoints and the underlying OS.

drozer provides tools to help you use, share and understand public Android exploits. It helps you to deploy a drozer Agent to a device through exploitation or social engineering. Using weasel (MWR's advanced exploitation payload) drozer is able to maximise the permissions available to it by installing a full agent, injecting a limited agent into a running process, or connecting a reverse shell to act as a Remote Access Tool (RAT).

drozer is open source software, maintained by WithSecure, and can be downloaded from [https://labs.withsecure.com/tools/drozer]


Building Using Android Studio
-----------------------------

1. Open Android Studio and launch the Android SDK manager from it (Tools > Android > SDK Manager)
2. Check that these two components are installed and updated to the latest version. Install or upgrade
   them if necessary.
    1. *Android SDK Platform Tools*
    2. *Android Support Library*
    3. *Google Play Services*
    4. *Google Repository*
    5. *Android NDK tools*
3. Return to Android Studio and select *Import Project*
4. Select the **drozer-agent** directory
5. Select "Import from existing model - Gradle"

**IMPORTANT**: Once imported make sure the project has been synchronized with the Gradle files.

License
-------

drozer is released under a 3-clause BSD License. See LICENSE for full details.

Contacting the Project
----------------------

drozer is Open Source software, made great by contributions from the community.

For full source code, to report bugs, or to suggest features and contribute patches please see our Github project:

[https://github.com/WithSecureLabs/drozer]
