# JSolar

JSolar, formerly JDiesel handles communication via the Drozer python client and the Android Drozer Agent.
This communication occurs over the protobuf defined in proto/protobuf.proto

The following packages are present in the codebase.

## api

With the api package, apart from some custom defined exceptions there are 2 classes.

**Device Info** provides a simple getter class, as well as constructor for holding information about the device

**Frame** When transmitting protocol buffers on-the-wire, we need a custom frame to indicate the length of the packet. This is an 8-byte header, containing two 32-bit, unsigned integers followed by the payload encoded as a protocol buffer:

```
| 0                   1                   2                   3   |
| 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 |
|                             version                             |
|                          payload length                         |
|                             payload                             |
|                               ...                               |
The byte order is little-endian.
```

The current protocol version number is 2. For compatibility purposes, some protocol version numbers are reserved. In particular:

Version 1
To avoid confusion with the original, XML-based protocol in documentation.
Version 60
To avoid problems if the Drozer v1 client connected to a newer Agent and started sending XML.
This header, theoretically, allows a max packet size of 4GiB. In practice this would cause a DoS condition on the Android device. A future version update may introduce a hard limit on the frame size.


### api/builders

The builders classes are java factories for constructing protobuf message objects from the direct values using reflection.
If adding new message types here and in the protobuf.proto file would be the places you would need the most coding.

**MessageFactory** is a wrapper providing polymorphic constructors for each of the message types.

**ReflectionResponseFactory** is the message factory which creates the reflection related messages (dark voodoo magic) needed to power Drozer
See [Reflection](#reflection) for what reflection is

**SystemRequestFactory** Handles the creation of Protobuf messages for requests from the agent to the Drozer client, this provides functionality for setting the device details, or binding / unbinding from the Drozer client

**SystemResponseFactory** Handles the creation of Protobuf messages for responses to the agents messages, this is fx: responding with a device list, or replying PONG to a ping message.


### api/connectors

See [Links](#api/links) I think?

### api/handlers

The message handlers are in charge of receiving messages from the Drozer Client and calling the relevant functionality.


**SystemMessageHandler** handles "System Request" messages, the is done by the main switch statement in the handle function.
Take fx: a list devices request from the Drozer Client. Here a response is built using the message factories defined in the builders. which returns the current device details

**ReflectionMessageHandler** like above but for reflection messages, most calls go through the reflector

### api/links

The links package implements the "Drozer Connections": (I think)
From the drozer wiki.
Connections
A Connection is a Java thread that implements the drozer Protocol on top of some Transport.
The Transport provides a general interface to send and receive Frame objects, as documented in the protocol.
The Connection receives messages from the Transport and passes them to a SystemMessageHandler, if they are a SYSTEM_REQUEST, or the appropriate Session if they are a REFLECTION_REQUEST.
Either the SystemMessageHandler or the session’s ReflectionRequestHandler will generate a message in response, which the Connection will forward back through the Transport to the Console.


### api/sessions

From the drozer wiki

Once a console has connected, regardless of whether it used direct or infrastructure mode, a Session is spawned in the Agent. A Session has a unique, 256-bit identifier, which must be included in all subsequent messages destined for the Session.
The Session provides a Queue, which receives all REFLECTION_REQUEST Message sent with the Session identifier. This queue is proactively polled, and all Message are passed to an instance of ReflectionMessageHandler, which processes the message, and invokes the required actions.
The Session also provides an ObjectStore, which the Reflector can use to stash objects when it is sending the object’s identifier to the Console.


### api/transport

Handles the creation of TLS Sockets, not too complex, won't bother explaining

### connection

Contains abstract classes and interfaces for the creation of connections, links and sessions
AbstractConnection is mostly documented and should be used as the main source.

### logger

Custom logger, has the ability of adding onLogMessageListeners, is mostly used by the agent itself.

### reflection

This is the main meat of the Drozer agent, from existing documentation "drozer exposes the underlying Dalvik VM to the Console by using Reflection. The Console can instantiate arbitrary Java classes, and interact with their properties and methods. This allows a module writer to write Java code in their module, using Python syntax."
While the above refers to the Dalvik VM android has for a long time now used the Android RunTime (ART) as the main runtime.
According to the official android documentation reflection should still be fine https://source.android.com/docs/core/runtime
However I get the feeling we might run into some weird issues down the line. oh well future WithSecure mobsec's problem.
https://developer.android.com/guide/practices/verifying-apps-art.html
In regards to the points here:
- Addressing garbage collection (GC) issues: No syscalls to GC are present
- Preventing JNI issues: No dangerous JNI usage, this should be checked with https://android-developers.googleblog.com/2011/07/debugging-android-jni-with-checkjni.html
- Preventing stack size issues: No threads are created specifying explicit stack sizes.
- Object model changes: No warnings provided by android studio, should be good
- Fixing AOT compilation issues: We arent doing fancy obfuscation, however good to keep in mind for the future


### util

3 classes in here.

**Shell**: This class implements a /bin/sh shell by reading and writing to stdin and stdout buffers.

**Strings**: Loads the "mstring" C++ library

**Verify**: provides an uncalled md5checksum function, seems useless