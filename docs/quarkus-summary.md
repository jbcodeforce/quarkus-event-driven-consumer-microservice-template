# Quarkus Summary

[Quarkus](https://quarkus.io/) is a Kubernetes Native Java stack tailored for OpenJDK HotSpot and GraalVM

## Create a template

Using [this web site](https://code.quarkus.io/), select the package and download.

## Using GraalVM

Start a container and verify `java -version` or `node`.

```shell
docker run -it -v $(pwd):/home oracle/graalvm-ce:20.0.0 bash
```

See [other usages here](https://www.graalvm.org/docs/getting-started/#docker-containers)

For python:

```shell
gu install python
graalpython
```

GraalVM can compile Java bytecode into native images to achieve faster startup and smaller footprint for your applications. 

```shell
$ javac HelloWorld.java
$ native-image HelloWorld
$ ./HelloWorld
```
## Quick list of common commands

See also this [Cheat sheer](https://lordofthejars.github.io/quarkus-cheat-sheet/#quarkuscheatsheet)