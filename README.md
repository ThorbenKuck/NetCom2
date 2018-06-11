# NetCom2

NetCom2 is a highly modular framework designed to function as a universal and asynchronous Client-Server-Communication-Interface.
It is designed to function as an over-network EventBus.

[![Build Status](https://travis-ci.org/ThorbenKuck/NetCom2.svg?branch=master)](https://travis-ci.org/ThorbenKuck/NetCom2) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.thorbenkuck/NetCom2/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.thorbenkuck/NetCom2) 
[![Known Vulnerabilities](https://snyk.io/test/github/thorbenkuck/NetCom2/badge.svg)](https://snyk.io/test/github/thorbenkuck/NetCom2) 
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ffbef87b4f3f44f6863096df9c87d0a0)](https://www.codacy.com/app/thorben.kuck/NetCom2?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ThorbenKuck/NetCom2&amp;utm_campaign=Badge_Grade)

## About this framework

This framework is designed to function as an over-network-EventBus. Whilst beeing lightweight, it still is extremly modular. Most bits can be changed in a reactive style. Though easy to use, you can accomplish many things using this.

#### The target source is java 1.8.    
#### The current version is compatible with java 1.9 (ONLY TESTED ROUGHLY!)

The 2 core-interfaces are:

<ul>
<li>ClientStart</li>
<li>ServerStart</li>
</ul>

and both can be instantiated via
```java
// Client instantiation
String address = ...
int port = ...
ClientStart clientStart = ClientStart.at(address, port);

// Server instantiation
int port = ...
ServerStart serverStart = ServerStart.at(port);
```

## For whom this is

If you are searching for a easy to use framework, that gives you the option to easily create a Server-Client-Communication, this is for you. Also you should consider taking a look at this, if you want to have a decoupled, yet easy to read over network communication.

But do not mistake this as an simple framework! If you want, you can create your own en-/decryption mechanism. You can create customized Serialization. This framework also comes with a pre-built cache and registration-mechanism, that makes it easier to send objects to specific clients who want those objects. This framework is completely modular, which means, you can change nearly everything, from serialization to the socket that is being used.

----

## Versions

The Framework-Versions are to be read like this:

A.B.C.D

A is a breaking, major release (feature-change)    
B is a non-breaking for normal use, potentially breaking for custom Instances, major release (feature-extension)    
C is a non-breaking, minor release (bug-fix)    
D is a non-breaking, security release    

Also there is a NIGHTLY branch, for the impatient.

### Current State

2.0 (release 30.03.2018)
 * Session lost its update and HeartBeat functions
 * ClientStart and ServerStart where updated.
   * Interface they use are now mostly located in network.shared
 * The Logging interface has been relocated to the root.logging package
   * This is more logical, but means, you have to adjust those imports.
 * Synchronization for sending Objects has been removed completely!
   * The reason is, that it is not needed most of the time, but always takes up resources.
 * All Default implementations have been renamed to Native[InterfaceName]
   * This should not effect anyone.
 * The Connection has been greatly rewritten.
 * The ServerStart no longer contains a Distributor.
   * To use it, state: `Distributor distributor = Distributor.open(serverStart)`
 * Keller has been integrated greatly
 * Session events have been removed
   * They accomplish the same behaviour as the CommunicationRegistration and are therefor not needed
 
 ### Installation
 
 Include this in your pom.xml (if you are using Maven)
 
 ```
 <dependency>
   <groupId>com.github.thorbenkuck</groupId>
   <artifactId>NetCom2</artifactId>
   <version>1.0.1</version>
 </dependency>
 ```
 
 Or this in your build.gradle (if you are using Gradle)
 
 ```
 dependencies {
     compile group: 'com.github.thorbenkuck', name: 'NetCom2', version: '1.0.1'
 }
 ```

## Getting started

If you are German, you might be interested in this [YouTube Tutorial-series](https://www.youtube.com/watch?v=YvyLHyt0k3k&list=PLUUnTdOVEgvIqNxqAUL8388A73Yzpn57E).    
If you cannot understand German, there is also a YouTube Tutorial-series in [English](https://www.youtube.com/watch?v=V33a8jRrp00&list=PLUUnTdOVEgvLKEQ7vD4Z3CL_0jb6u__ay), but this might sound a bit odd.

### Mini tutorial

Getting started is easy. You should have basic knowledge about how a Client-Server-Architecture works. For that example, let's assume you have 3 Project: Client, Server and Shared, while Client and Server depend on Shared.

#### Creating a Server

To create a server, you simply say:

```java
ServerStart serverStart = ServerStart.at(/* your port number here */88888);
```

With that done, you have to tell the ServerStart-Object to listen to Clients

```java
try {
  serverStart.launch();
  serverStart.acceptAllNextClients();
} catch (/* ClientConnectionFailedException | StartFailedException */ NetComException e) {
  e.printStackTrace();
  System.exit(1);
}
```

Launch creates internal dependencies and acceptAllNextClients(); waits for the next clients to connect.

#### Creating a Client

You create a Client similar to a Server. You just say:

```java
ClientStart clientStart = ClientStart.at(/* address of Server */"localhost", /* port of Server*/88888);
```

Now, to connect, simply say:

```java 
clientStart.launch();
```

#### Sending Stuff

Let's assume, you have an Object called Test in the Shared project, which looks like this:

```java
public class Test implements Serializable {
  private String s;
  
  public void setString(String newS) {
    this.s = newS;
  }
  
  public String getString() {
    return this.s;
  }
}
```

Now we want to send this from the Client to the Server. We realize this by saying:

```java
ClientStart clientStart = ClientStart.at(/* address of Server */"localhost", /* port of Server*/88888);
clientStart.launch();
clientStart.send().objectToServer(new Test());
```

on the ServerSide we have to say, how to handle this Object. We realize this by saying:

```java
ServerStart serverStart = ServerStart.at(88888);
serverStart.launch();

serverStart.getCommunicationRegistration().register(Test.class).addFirst((session, o) -> {
  System.out.println("received " + o.getString() + " from " + session);
  o.setString("received");
  session.send(o);
});

try {
  serverStart.acceptAllNextClients();
} catch (ClientConnectionFailedException e) {
  e.printStackTrace();
  System.exit(1);
}
```

There you go, you have a simple Server, that prints out what he received to the console and sends a "received" message back.

## Where to go from here

Every module from NetCom2 has some sort of modular base. This might sound overwhelming, but in fact, this Framework provides a vast amount of default implementation, which make it very easy to get started.

Check out the [Wiki](https://github.com/ThorbenKuck/NetCom2/wiki) for more information's about creating a Server and a Client, with more depth.

If you want to see someone code using NetCom2 and understand german, check out [this Let's Code-series](https://www.youtube.com/watch?v=b8y5eJbmUvs&list=PLUUnTdOVEgvKSiaWfWuhwLJfmwZIHkvGV).

----
#### Current Development

__We are currently looking for a good, free and reliable board for new Feature suggestions__

Found a bug? Missing a Test? Report it as a [issue](https://github.com/ThorbenKuck/NetCom2/issues).    
Don't know where to start? Look at the [github-wiki](https://github.com/ThorbenKuck/NetCom2/wiki), in particular the examples.     
Have Problems starting? Post your question on [StackOverflow](https://stackoverflow.com/questions/ask?tags=java+NetCom2) and add the NetCom2 tag.

#### What comes next?

First off, we would like to get this framework out there. Future changes are not planned yet, though we have some ideas. But note, it is not certain, whether or not, they will be added. We are looking for a board, to provide you with a way of suggesting new additions to this framework.

Found a bug? Missing a Test? Report it as a [issue](https://github.com/ThorbenKuck/NetCom2/issues).    
Don't know where to start? Look at the [github-wiki](https://github.com/ThorbenKuck/NetCom2/wiki), in particular the examples.     
Have Problems starting? Post your question on [StackOverflow](https://stackoverflow.com/questions/ask?tags=java+NetCom2) and add the NetCom2 tag.    
If you are having any other trouble with NetCom2, please also turn to StackOverflow.
          
----

