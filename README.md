# KumuluzEE REST
[![Build Status](https://img.shields.io/travis/kumuluz/kumuluzee-rest/master.svg?style=flat)](https://travis-ci.org/kumuluz/kumuluzee-rest)

> KumuluzEE REST automatically exposes your JPA entities as a REST service with build-in metadata and query support using JAX-RS.

## Usage

You can download the binaries from the [releases page](https://github.com/kumuluz/kumuluzee-rest/releases).

The project is split into two modules `core` and `rest`. The former contains all the main utilities, classes and
helpers that use JPA to manipulate the data. The latter module contains the generic JAX-RS classes that use the core
module to automatically expose your entities as a REST services. If you only want the utilities that are used behind
the scenes and don't want to expose your entities as a REST service (or you want to do it on your own) only include
the `core` module. However if you want to use everything then only include the `rest` module.

## Getting started

TODO

## Building

Ensure you have JDK 8 (or newer), Maven 3.2.1 (or newer) and Git installed

```bash
    java -version
    mvn -version
    git --version
```

First clone the KumuluzEE REST repository:

```bash
    git clone https://github.com/kumuluz/kumuluzee-rest.git
    cd kumuluzee-rest
```
    
To build KumuluzEE REST run:

```bash
    mvn install
```

This will build all modules and run the testsuite. 
    
Once completed you will find the build archives in the modules respected `target` folder.

## Changelog

Recent changes can be viewed on Github on the [Releases Page](https://github.com/kumuluz/kumuluzee-rest/releases)

## Contribute

See the [contributing docs](https://github.com/kumuluz/kumuluzee-rest/blob/master/CONTRIBUTING.md)

When submitting an issue, please follow the [guidelines](https://github.com/kumuluz/kumuluzee-rest/blob/master/CONTRIBUTING.md#bugs).

When submitting a bugfix, write a test that exposes the bug and fails before applying your fix. Submit the test alongside the fix.

When submitting a new feature, add tests that cover the feature.

## License

MIT