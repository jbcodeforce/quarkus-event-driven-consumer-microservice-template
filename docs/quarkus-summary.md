# Quarkus Summary

[Quarkus](https://quarkus.io/) is a Kubernetes Native Java stack tailored for OpenJDK HotSpot and GraalVM.

## Create a template

Using [this web site](https://code.quarkus.io/), select the package and download.

## Run development mode

`quarkus:dev` runs Quarkus in development mode. This enables hot deployment with background compilation, which means that when you modify your Java files and/or your resource files and refresh your browser, these changes will automatically take effect.


## Using GraalVM

You can use a multi-stage Docker build to run Maven inside a Docker container that embeds GraalVM. 

Start a container and verify `java -version` or `node`.

```shell
docker run -it -v $(pwd):/home -p 8080:8080 oracle/graalvm-ce:20.0.0 bash
```

See [other usages here](https://www.graalvm.org/docs/getting-started/#docker-containers)

For python, in the bash session in the graalvm container:

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
When using maven in the container, the internal maven repository will be update when doing `mvn package`. We can do a `docker commit <containerid> jbcodeforce/newgraalvm` to keep those installation between container sessions, and use this image for future runs.



## Microprofile app with quarkus

Start by creating a microprofile base code with [Microprofile starter](https://start.microprofile.io/).

Replace the "starter" maven `pom.xml` file with a new one, created for Quarkus with the following command (verify quarkus maven plugin version [in mvn repository](https://mvnrepository.com/artifact/io.quarkus/quarkus-maven-plugin)):

```
mvn io.quarkus:quarkus-maven-plugin:1.2.1.Final:create -DprojectGroupId=ibm.gse.eda -DprojectArtifactId=demo -Dextensions="smallrye-health, smallrye-metrics, smallrye-openapi, smallrye-fault-tolerance, smallrye-jwt, resteasy, resteasy-jsonb, arc"
```

## CDI

CDI is supported by ArC and is part of the `quarkus-resteasy` extension. Variable injection is done with `@Inject` and `@ConfigProperties`. The order is properties < System.getEnv()  < System.getProperties()

```java
@Inject 
@ConfigProperty(name = "main.topic.name")
protected String mainTopicName;
```
## Application initialization

As part of the consumer implementation, is the need to start the consumer as soon as the application is started. Quarkus support two events: `StartupEvent, ShutdownEvent` that can be processed:

```java
void onStart(@Observes StartupEvent ev) {               
        LOGGER.info("The application is starting...");
    }

    void onStop(@Observes ShutdownEvent ev) {               
        LOGGER.info("The application is stopping...");
    }
```
## Quick list of common commands

See also this [Cheat sheer](https://lordofthejars.github.io/quarkus-cheat-sheet/#quarkuscheatsheet)