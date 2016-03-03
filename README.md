# RepositoryCache
[![Apache 2.0](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0) 
[![Android](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)
[![Java](https://img.shields.io/badge/platform-java-red.svg)](http://www.oracle.com/technetwork/java/javaee/overview/index.html)

Annotation Processor that aims to help developers to get an easy strategy for their repositories.

This is a Java library that can be imported both in Android or Java projects.

**Features:**
  - Fast implementation for each repository method.
  - Provides simple methods like isCached or isExpired.
  - Distinguish between different calls of the same method.
  - Provides the ability to store content on disk without the need of a database.



## Dependency

Latest stable version: 
[![Latest Version](https://api.bintray.com/packages/kuassivi/maven/repository-cache/images/download.svg) ](https://bintray.com/kuassivi/maven/repository-cache/_latestVersion)
[![Bintray Version](https://img.shields.io/bintray/v/kuassivi/maven/repository-cache.svg)](https://dl.bintray.com/kuassivi/maven/com/kuassivi/annotation/repository-cache/)
[![Maven Central](https://img.shields.io/maven-central/v/com.kuassivi.annotation/repository-cache.svg)]()

In order to generate the proxy classes when building your project, 
you need to apply the [android-apt] gradle plugin to run annotation processing.

If you are working with gradle, add the dependency to your build.gradle file:
```groovy
dependencies{
    compile 'com.kuassivi.annotation:repository-cache:?.?.?'
    apt 'com.kuassivi.compiler:repository-cache-compiler:?.?.?'
}
```
If you are working with maven, do it into your pom.xml
```xml
<dependency>
    <groupId>com.kuassivi.annotation</groupId>
    <artifactId>repository-cache</artifactId>
    <version>?.?.?</version>
    <type>pom</type>
</dependency>
<dependency>
    <groupId>com.kuassivi.compiler</groupId>
    <artifactId>repository-cache-compiler</artifactId>
    <version>?.?.?</version>
    <type>pom</type>
</dependency>
```



## How to:

Suppose you often define some contracts for your repositories.
If so then you can annotate each of your methods with the `RepositoryCache` annotation and provide a time to expire.

Regular classes, not just interfaces, can be annotated as well.

Lets see an example:

```java
public interface MyRepository {

    @RepositoryCache(1000 * 60 * 60) // expiration time in ms.
    Object getDataById(int id);
    
}
```

> Now **build** your project.

> Only Repository interfaces with annotated methods will be processed.

Once you have built the project, a new Java Proxy Class will be created in your build source `apt` folder.

The Proxy Class generated will be a kind of: _InterfaceClassName_ + _ProxyCache_ suffix.

For instance, `MyRepositoryProxyCache`.

Lets see how to use the Proxy Class:

```java
public class MyRepositoryImpl implements MyRepository {

    @Override
    public Object getDataById(int id) {
    
        // Gets an instance of the cached method
        RepositoryProxyCache cache = MyRepositoryProxyCache.getDataById(context.getCacheDir());
        
        // Ensure we are getting distinct cache instance per id.
        cache.select( id );
        
        // Checks whether it is cached and it is not expired.
        if (!cache.isExpired()) {
        
            // [...] proceed retrieving data from your local store (maybe Realm database)
            
        } else {
        
            // [...] otherwise proceed retrieving data from your cloud store (maybe a rest service)
            
            // Once you persist, it will store this call with the specified id in the cache.
            cache.persist();
            
        }
    }
    
}
```



## Tips:

> \- Use always an Interface as a contract. It is much clean.

> \- You can cache a method indefinitely, simply do not put any time on the annotation.

> \- Remember to distinguish between calls with an ID or any other logic through `select(id)`.

> \- You can store content on the cache with `persist(String)` if you are not planning to have a database.
Just transform your Object from/to a Json string and retrieve that content with `getContent()`.




## _ProxyCache_ Methods:

 *     `select(Object)` - distinguish between different calls of the same method, the parameter should be any kind of id.
 *     `isCached()` - returns true if a method call is cached, false otherwise.
 *     `isExpired()` - returns true if a method call is not cached or is expired, false otherwise.
 *     `persist()` - stores a method call in the cache.
 *     `persist(String)` - stores a method call in the cache with the provided content.
 *     `getContent()` - retrieves the stored content of a specific method call.
 *     `evict()` - removes a specific method call from the cache.



## _RepositoryCacheManager_ Methods:

 *     `static hashMD5(String)` - generates a MD5 hash string of the provided String parameter.
 *     `static hashCode(Object...)` - generates a hash code from the provided parameter objects.
 *     `static evictAll(File)` - removes all cache repositories. You must provide the cache directory.
 
 
 
## License

Copyright 2016 Francisco Gonzalez-Armijo Riádigos

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.



[android-apt]: https://bitbucket.org/hvisser/android-apt
