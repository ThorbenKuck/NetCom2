# NetCom2

NetCom2 is a highly modular framework designed to function as a universal and asynchronous Client-Server-Communication-Interface. It is designed to function as an over-network EventBus.

Learning Resources: [**Wiki**](https://github.com/ThorbenKuck/NetCom2/wiki), [**Java-Doc**](https://thorbenkuck.github.io/NetCom2/apidocs/)

[![Build Status](https://travis-ci.org/ThorbenKuck/NetCom2.svg?branch=master)](https://travis-ci.org/ThorbenKuck/NetCom2) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.thorbenkuck/NetCom2/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.thorbenkuck/NetCom2) 
[![Known Vulnerabilities](https://snyk.io/test/github/thorbenkuck/NetCom2/badge.svg)](https://snyk.io/test/github/thorbenkuck/NetCom2) 
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ffbef87b4f3f44f6863096df9c87d0a0)](https://www.codacy.com/app/thorben.kuck/NetCom2?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ThorbenKuck/NetCom2&amp;utm_campaign=Badge_Grade)

### Mini tutorial

Getting started is easy. You should have basic knowledge about how a Client-Server-Architecture works. For that example, let's assume you have 3 Project: Client, Server and Shared, while Client and Server depend on Shared.

#### Creating a Server

To create a server, you simply say:

```java
// Create a ServerStart-Object
// respresentitive for the actual server
ServerStart serverStart = ServerStart.at(/* your port number here */88888);
```

With that done, you have to tell the ServerStart-Object to listen to Clients

```java
// Create internal dependencies and reserve the port for the Server
serverStart.launch();
// Listen for all connecting Clients
// This method call will block until the ServerStart is closed
serverStart.acceptAllNextClients();
```

Launch creates internal dependencies and acceptAllNextClients(); waits for the next clients to connect.

#### Creating a Client

You create a Client similar to a Server. You just say:

```java
// Create a ClientStart-Object
// respresentitive for an actual client
ClientStart clientStart = ClientStart.at(/* address of Server */"localhost", /* port of Server*/88888);
```

Now, to connect, simply say:

```java
// Create the Sockets and actually connect to the server
// At this point, the ServerStart has to be (at least) launched
// This call will block until the ServerStart#acceptNextClient method handles this ClientStart
clientStart.launch();
```

#### Sending Stuff

Let's assume, you have an Object called Test in the Shared project, which looks like this:

```java
/**
 * implements Serializable is responsible for the serialzation
 * By default, NetCom2 uses the Java-Serialzation, so without this import,
 * this object cannot be serialized (note though, that you could provide
 * custom Serialzation)
 */
public class Test implements Serializable {
  private String s;
  
  public String getString() {
    return this.s;
  }
}
```

Now we want to send this from the Client to the Server. On the Server-Side, we want to simply print out what we received. The full examples look like this:

#### Full Client example

```java
// Create a ClientStart object
ClientStart clientStart = ClientStart.at(/* address of Server */"localhost", /* port of Server*/88888);

try {
  // Try to connect to the server
  // The ServerStart has to be (at least) launched at this point
  clientStart.launch();
} catch (StartFailedException e) {
  // This Exception means, that the Server is not reachable for any reason
  e.printStackTrace();
  System.exit(1);
}
// Create a Module, that allows us to send objects to the connected Server
Sender sender = Sender.open(clientStart);
// actually send a Test to the Server
sender.objectToServer(new Test());
```

#### Full Server example

```java
// Create the Server-Object
ServerStart serverStart = ServerStart.at(88888);

try {
  // Create internal dependencies and reserve the port for the Server
  serverStart.launch();
} catch(StartFailedException e) {
  // Handling has to be adjusted!
  e.printStackTrace();
  System.exit(1);
}

// The CommunicationRegistration will maintain ReceivePipeline instance
// Those will be triggered, once an object is received.
serverStart.getCommunicationRegistration()
     .register(Test.class)        // Register the Test-Class and create a ReceivePipeline for it
     .addFirst((session, o) -> {  // Add a println to the head of the ReceivePipeline 
          System.out.println("received " + o.getString() + " from " + session);
        });

try {
  // Handle all Clients, that want to connect.
  serverStart.acceptAllNextClients();
} catch (ClientConnectionFailedException e) {
  // Handling has to be adjusted!
  e.printStackTrace();
  System.exit(1);
}
```

First run the Server example and then run the Client example. Within the console of the Server, you will see an output the a Test was received.

This is a pretty simple example, make sure to chek out the [wiki](https://github.com/ThorbenKuck/NetCom2/wiki), espacially the "example" section to get more concrete examples. All components are explained within the rest of the wiki.

### Installation
 
 Include this in your pom.xml (if you are using Maven)
 
 ```
 <dependency>
   <groupId>com.github.thorbenkuck</groupId>
   <artifactId>NetCom2</artifactId>
   <version>2.0</version>
 </dependency>
 ```
 
 Or this in your build.gradle (if you are using Gradle)
 
 ```
 dependencies {
     compile group: 'com.github.thorbenkuck', name: 'NetCom2', version: '2.0'
 }
 ```

**The target source is java 1.8.+**

## For whom this is

If you are searching for a easy to use framework, that gives you the option to easily create a Server-Client-Communication, this is for you. Also you should consider taking a look at this, if you want to have a decoupled, yet easy to read over network communication.

But do not mistake this as an simple framework! If you want, you can create your own en-/decryption mechanism. You can create customized Serialization. This framework also comes with a pre-built cache and registration-mechanism, that makes it easier to send objects to specific clients who want those objects. This framework is completely modular, which means, you can change nearly everything, from serialization to the socket that is being used.

## Currently...

*The Version 2.0 has been release!*

The unit tests are not within this package. They will be back with the next security upate.

If you want to migrate to the new Version, check out our [V.2 Migration Guide](https://github.com/ThorbenKuck/NetCom2/wiki/V.2-Migration-Guide)

## Getting started

German Tutorials: [YouTube Tutorial-series](https://www.youtube.com/watch?v=YvyLHyt0k3k&list=PLUUnTdOVEgvIqNxqAUL8388A73Yzpn57E).    
English Tutorials: [YouTube Tutorial-series](https://www.youtube.com/watch?v=V33a8jRrp00&list=PLUUnTdOVEgvLKEQ7vD4Z3CL_0jb6u__ay)

## Where to go from here

Every module from NetCom2 has some sort of modular base. This might sound overwhelming, but in fact, this Framework provides a vast amount of default implementation, which make it very easy to get started.

If you want to see someone code using NetCom2 and understand german, check out [this Let's Code-series](https://www.youtube.com/watch?v=b8y5eJbmUvs&list=PLUUnTdOVEgvKSiaWfWuhwLJfmwZIHkvGV).

Check out the [Wiki](https://github.com/ThorbenKuck/NetCom2/wiki) for more information's about creating a Server and a Client, with more depth.

Also, you may be interrested in checking out the Java-Doc, which can be find [here](https://thorbenkuck.github.io/NetCom2/apidocs/)

----
## Current Development

__We are currently looking for a good, free and reliable board for new feature suggestions__

Found a bug? Missing a Test? Report it as a [issue](https://github.com/ThorbenKuck/NetCom2/issues).    
Don't know where to start? Look at the [github-wiki](https://github.com/ThorbenKuck/NetCom2/wiki), in particular the examples.     
Have Problems starting? Post your question on [StackOverflow](https://stackoverflow.com/questions/ask?tags=java+NetCom2) and add the NetCom2 tag.

### Versions

The Framework-Versions are to be read like this:

A.B.C.D

A is a breaking, major release (feature-change)    
B is a non-breaking for normal use, potentially breaking for custom Instances, major release (feature-extension)    
C is a non-breaking, minor release (bug-fix)    
D is a non-breaking, security release    

Also there is a NIGHTLY branch, for the impatient.

#### Current State

2.0 (release 19.08.2018)
 * Session lost its update and HeartBeat functions
   * Update is a security risk. The Session should not be send through the Network
   * Heartbeats make no sens connected to the Session
 * ClientStart and ServerStart where updated.
   * Interface they use are now mostly located in network.shared
 * The Logging interface has been relocated to the root.logging package
   * This is more logical, but means, you have to adjust those imports.
 * Synchronization for sending Objects has been removed completely!
   * The reason is, that it is not needed most of the time, but always takes up resources.
 * All Default implementations have been renamed to Native[InterfaceName]
   * This should not effect anyone.
 * The Connection has been greatly rewritten.
   * The interface was advanced and changed
   * There are now Connections, that use Socket, SocketChannel and DatagrammSocket.
   * Within the OnReceiveTripple, the Connection is no longer used
     * Instead, a ConnectionContext has been introduced
     * This ConnectionContext combines the Connection and the representing Client
 * The Client has been greatly rewritten.
   * The Client no longer follows the old setup-mechanism
   * The definition of the send method was changed
     * The Object that should be send comes before the Connection
   * The Client now has a sendIgnoreConstraints method
     * Normal send methods wait until a Connection is successfully established, sendIgnoreConstraints does not.
 * Some classes have been decoupled by default.
   * All of the associated methods are still prevalent in the respective interfaces.
   * They are deprecated and will be removed with the next major update
   * The ServerStart no longer contains a Distributor.
     * To use it, state: `Distributor distributor = Distributor.open(serverStart)`
   * The ServerStart no longer contains a RemoteObjectRegistration
     * To use it, state: `RemoteObjectRegistration registration = RemoteObjectRegistraion.open(ServerStart)`
   * The ClientStart no longer contains a Sender
     * To use it, state: `Sender sender = Sender.open(clientStart)`
   * The ClientStart no longer contains a RemoteObjectFactory
     * To use it, state: `RemoteObjectFactory factory = RemoteObjectFactor.open(clientStart)`
     * Also, the Method `ClientStart#updateRemoteInvocationProducer` will throw an UnsupportedOperationException. This is due to the fact, that the ClientStart no longer contains a RemoteObjectFactory
 * Keller has been integrated greatly
 * Session events have been removed
   * They accomplish the same behaviour as the CommunicationRegistration and are therefor not needed
 * The Thread-Management has been reworked.
   * The NetComThreadPool takes tasks, that will be worked on by worker-tasks
   * You can submit custom worker tasks (like submitting a Runnable to a ExecutorService)
 * ServiceDiscovery was introduced
