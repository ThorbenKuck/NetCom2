# NetCom2-Auto

NetCom2-Auto is a module which introduces annotation-processor based approach to reduce boilerplate code.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.thorbenkuck/NetCom2-Logging/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.thorbenkuck/NetCom2-Logging) 

## About this module

Annotation processors generate code that will be handled internally and introduced automatically into ServerStart and ClientStarts if wanted.

All you have to do, is to annotate certain methods with specific annotations. You create a ServerStart/ClientStart using a new Class called ```NetCom2```. This will collect the gernerated classes (if wanted).

## Dependencies to other Modules

Depends on the [Main-Module](https://github.com/ThorbenKuck/NetCom2/tree/master/main).

## Examples

First off, we create a ServerStart example. For that, we can use the following annotations:

```java
// An annotation, that calls the annotated method
// once the declared object is received.
// Optional: Add the ConnectionContext and Session
// or only the Session as an parameter.
// Note: They have to be declared as following:
// [Object], [Session, Object], [ConnectionContext, Session, Object]
@Register

// The annotated method is called, once a new
// ClientStart connects to the ServerStart
@Connect

// The annotated method is called, once a
// ClientStart disconnects from the ServerStart
@Disconnect

// The annotated method has to have the ServerStart
// as an parameter. This method is than called
// before the ServerStart is launched.
@Configure
```

Now, all we have to do, is to:
1. Write our methods
2. Add the correct annotations
3. Create a ServerStart using the `NetCom2` class

A complete Example can be seen here:

```java
public class ServerExample {

    public static void main(String[] args) {
        new ServerExample().run();
    }
    
    public void run() {
        NetCom2.launchServer()
             .use(ObjectRepository.hashingRecursive())
             .at(8888)
             .onCurrentThread();
    }
    
    @Register
    public void receive(/*ConnectionContext context, */Session session, TestObject testObject) {
        // Handle the received TestObject
        session.send(testObject);
    }
    
    @Connect
    public void connect(Client client) {
        // Handle the connect of a new client
    }
    
    @Disconnect
    public void connect(Client client) {
        // Handle the disconnect of a client
    }
    
    @Configure
    public void configure(ServerStart serverStart) {
        // Configure a ServerStart before it is launched
    }
}
```

On the Client-Side we have the same annotations, but without the `@Connect` annotation. The do the same as on the Server-Side. The `@Connect` annotation is not needed, because once the ClientStart#launch method is called, we are connected.

## For whom this is

For nearly everyone that writes code using NetCom2. This module greatly reduces the boilerplate code.


## Current State

1.0 Released.
  - Added annotations
    - `@Register`
    - `@Connect`
    - `@Disconnect`
    - `@Configure`
 
 ### Installation
 
 Include this in your pom.xml (if you are using Maven)
 
 ```
 <dependency>
   <groupId>com.github.thorbenkuck</groupId>
   <artifactId>NetCom2-Auto</artifactId>
   <version>1.0</version>
 </dependency>
 ```
 
 Or this in your build.gradle (if you are using Gradle)
 
 ```
 dependencies {
     compile group: 'com.github.thorbenkuck', name: 'NetCom2-Auto', version: '1.0'
 }
 ```
