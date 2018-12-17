# NetCom2-Cache

This module introduces a low-level and observable cache

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.thorbenkuck/NetCom2-Cache/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.thorbenkuck/NetCom2-Cache) 

## About this module

The Cache-implementation introduced in this package is very low level. Dad tongues may call it a fancy wrapper for a HashMap.

This Cache can simply be extended through observables. Those observables may introduce a timeout or something, but the target of this specific module was a very low-level cache. No automated data-maintenance.

## Dependencies to other Modules

[NetCom2-Logging](https://github.com/ThorbenKuck/NetCom2/tree/master/logging)    
[NetCom2-Utils](https://github.com/ThorbenKuck/NetCom2/tree/master/utils)

## Examples

```java
class ExampleObserver extends AbstractCacheObserver<TestObject> {
	ExampleObserver() {
		super(TestObject.class);
	}
	
	@Override
	public void newEntry(final TestObject t, final CacheObservable observable) {
		System.out.println("New TestObject added to cache");
	}

    @Override
	public void updatedEntry(final TestObject t, final CacheObservable observable) {
		System.out.println("Updated TestObject added to cache");
	}

    @Override
	public void deletedEntry(final TestObject t, final CacheObservable observable) {
		System.out.println("Deleted TestObject from cache");
	}
}

public class Example {
	public void run() {
        Cache cache = Cache.open();
        TestObject object = new TestObject();
        cache.addCacheObserver(new ExampleObserver());
        
        cache.addNew(object); // Only if type is not stored
        cache.update(object); // Only if type is stored
        cache.addAndOverride(object); // Both of the above
        
        cache.remove(TestObject.class); // remove all instances of the type
    }
}
```

The output of this Example (if the run method is called) will be:

```
New TestObject added to cache
Updated TestObject added to cache
Updated TestObject added to cache
Deleted TestObject from cache
```

## For whom this is

This module is for you, if you need a observable Map which can differentiate between new additions, updates and removals.

## Current State

1.0 Released as separate module
  - removed deprecated methods
 
 ### Installation
 
 Include this in your pom.xml (if you are using Maven)
 
 ```
 <dependency>
   <groupId>com.github.thorbenkuck</groupId>
   <artifactId>NetCom2-Cache</artifactId>
   <version>1.0</version>
 </dependency>
 ```
 
 Or this in your build.gradle (if you are using Gradle)
 
 ```
 dependencies {
     compile group: 'com.github.thorbenkuck', name: 'NetCom2-Cache', version: '1.0'
 }
 ```
