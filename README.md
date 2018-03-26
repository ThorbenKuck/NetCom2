# NetCom2

NetCom2 is a framework, highly modular desigend to function as a universal and asynchronus Client-Server-Communication-Interface.
It is designed to function as an over-network EventBus.

[![Build Status](https://travis-ci.org/ThorbenKuck/NetCom2.svg?branch=master)](https://travis-ci.org/ThorbenKuck/NetCom2)    
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.thorbenkuck/NetCom2/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.thorbenkuck/NetCom2)    
[![Known Vulnerabilities](https://snyk.io/test/github/thorbenkuck/cliparser/badge.svg)](https://snyk.io/test/github/thorbenkuck/cliparser)    
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ffbef87b4f3f44f6863096df9c87d0a0)](https://www.codacy.com/app/thorben.kuck/NetCom2?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ThorbenKuck/NetCom2&amp;utm_campaign=Badge_Grade)

## Near future releases:

__Version 1.0 will be released on the 30th of march.__

Once Version 1.0 is released, it will be published at [Twitter](https://twitter.com/ThorbenKu) and potentially on [Google+](https://plus.google.com/u/0/+ThorbenKuck).

Preparations are mildly needed. Since all Versions 0.x are beta-versions, you will need to look close, whether or not, you import the correct class/interface. No other major-breaks, appart from logical package-relocations, have been introduced. All interfaces are still the same, some have just changed theire location.

The only thing that realy did change in interfaces is, that methods annotated with @Deprecated have been removed! No Exceptions!

Appart from relocations, this Version fixes some bugs, that no one had discovered yet.

The APIs had been worked out and tested in 450 Tests.

#### What comes next?

First of, we would like to get this framework out there. Future changes are not planed yet, tho we have some ideas. But note, it is not certain, whether or not, they will be added. We are looking for a board, to provide you with a way of suggesting new additions to this framework.

Found a bug? Missing a Test? Report it as a [issue](https://github.com/ThorbenKuck/NetCom2/issues).    
Have Problems starting? Look at the [github-wiki](https://github.com/ThorbenKuck/NetCom2/wiki) or post on [StackOverflow](https://stackoverflow.com/questions/ask?tags=java+NetCom2) and add the NetCom2 tag.    
If you are having some other trouble with NetCom2, please also turn to StackOverflow.
          
----

## About this framework

This framework is designed to function as an over-network-EventBus. Whilst beeing lightweight, it still is extremly modular. Most bits can be changed in a reactive style. Tho easy to use, you can acomplish many thing using this.

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

The Framework-Versions are to be red like this:

A.B.C.D

A is a breaking, major releas (feature-change)    
B is a non-breaking for normal use, potentially breaking for custom Instances, major release (feature-extention)    
C is a non-breaking, minor release (bug-fix)    
D is a non-breaking, security release    

Also there is a NIGHTLY branch, for the inpatient.

### Current State

0.4.0.0_BETA (Release 11.01.2017)
 * Further Documentation
 * Greater Thread-Safety and Performance
 * RemoteObjects
 * Connection-Routes
 * Bug-Fixes
 
0.3.0.0_BETA (Release 09.07.2017)

#### Current Development

 * Completion of JavaDOC
 * Connection Routes

## For whom this is

If you are searching for a easy to use framework, that gives you the option to fastly create a Server-Client-Communication, this is for you. Also you should consider taking a look at this, if you want to have an decoupled, yet easy to read over network communication.

But do not mistake this as an simple framework! If you want, you can create your own en-/decryption mechanism. You can create customized Serialization. This framework also comes with a pre-build cache and registration-mechanism, that makes it easyer to send Object to specific clients, that want those object. This framework is completly modular, which means, you can change nearly everything, from serialization to the socket, that is being used.

----

## Getting started

If you are german, you might be interrested in this [YouTube Tutorial-series](https://www.youtube.com/watch?v=YvyLHyt0k3k&list=PLUUnTdOVEgvIqNxqAUL8388A73Yzpn57E).    
If you cannot understand german, there is also a YouTube Tutorial-series in [english](https://www.youtube.com/watch?v=V33a8jRrp00&list=PLUUnTdOVEgvLKEQ7vD4Z3CL_0jb6u__ay), but this might sound a bit odd.

### Installation

Include this in your pom.xml (if you are using Maven)

```
<dependency>
  <groupId>com.github.thorbenkuck</groupId>
  <artifactId>NetCom2</artifactId>
  <version>0.4</version>
</dependency>
```

Or this in you build.gradle (if you are using Gradle)

```
dependencies {
    compile group: 'com.github.thorbenkuck', name: 'NetCom2', version: '0.4'
}
```

### Starting to code

Getting started is easy. You should have basic knowledge about how a Client-Server-Architecture works. For that excample, let's assume you have 3 Project: Client, Server and Shared, while Client and Server depend on Shared.

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

You create a Client similiiar to a Server. You just say:

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

Now, you have a simple Server, that prints out what he received to the console and sends a "received" message back.

## Where to go from here

Check out the Wiki for more informations about creating a Server and a Client, with more depth.

If you want to see someone code using NetCom2 and understand german, check out [this Let's Code-series](https://www.youtube.com/watch?v=b8y5eJbmUvs&list=PLUUnTdOVEgvKSiaWfWuhwLJfmwZIHkvGV).

----
