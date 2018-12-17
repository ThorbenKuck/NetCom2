# NetCom2-Asynch

NetCom2-Asynch is a small module, that introduces a ThreadPool system, used within NetCom2

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.thorbenkuck/NetCom2-Asynch/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.thorbenkuck/NetCom2-Asynch) 

## About this module

This module introduces 3 classes. One specific NetComThread, one NetComThreadFactory to create NetComThreads and a NetComThreadPool.

The NetComThreadPool is a very specific worker ThreadPool, that contains only "slave-Threads". Those slave-Threads will run worker-runnables, that execute other Runnable instances.

## Dependencies to other Modules

[NetCom2-Logging](https://github.com/ThorbenKuck/NetCom2/tree/master/logging)    
[NetCom2-Utils](https://github.com/ThorbenKuck/NetCom2/tree/master/utils)

## Examples

```java
// Both of these calls, will submit
// the runnables to be handled by
// all internally running 
// slave-threads. They will not be
// passed to a new Thread, but to
// already running slave instances.
// Those calls will only create
// new Threads, if not enough 
// slave-threads are running
NetComThreadPool.submit(() -> {
	// Asynchronous task
})
NetComThreadPool.submitPriorityTask(() -> {
	// Asynchronous high priority task
})

// This call however will most 
// likely create a new Thread 
// and start it. This Thread will
// handle the submitted Runnable.
// This Runnable will NOT be 
// handled by a slave-thread!
NetComThreadPool.submitCustomProcess(() -> {
	// Our custom process
})

// A simple output of how many 
// slave-threads are running 
// and how many may be
// running in parallel
System.out.println(NetComThreadPool.generateDiagnosticOutput());
```

## For whom this is

This module is used to introduce parallel working with not that many resources. Encapsulating a ExecutorService, this module is very specific. Most likely, this module is not what you want.

But, if you want to execute sequential operations in parallel without a great overhead, this module may be something for you.


## Current State

1.0 Released as separate module
  - removed deprecated methods
 
 ### Installation
 
 Include this in your pom.xml (if you are using Maven)
 
 ```
 <dependency>
   <groupId>com.github.thorbenkuck</groupId>
   <artifactId>NetCom2-Asynch</artifactId>
   <version>1.0</version>
 </dependency>
 ```
 
 Or this in your build.gradle (if you are using Gradle)
 
 ```
 dependencies {
     compile group: 'com.github.thorbenkuck', name: 'NetCom2-Asynch', version: '1.0'
 }
 ```
