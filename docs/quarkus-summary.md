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
javac HelloWorld.java
native-image HelloWorld
./HelloWorld
```

When using maven in the container, the internal maven repository will be update when doing `mvn package`. We can do a `docker commit <containerid> jbcodeforce/newgraalvm` to keep those installation between container sessions, and use this image for future runs.

One of the limitations of GraalVM is the usage of Reflection. Reflective operations are supported but all relevant members must be registered for reflection explicitly.

## Microprofile app with quarkus

Start by creating a microprofile base code with [Microprofile starter](https://start.microprofile.io/).

Replace the "starter" maven `pom.xml` file with a new one, created for Quarkus with the following command (verify quarkus maven plugin version [in mvn repository](https://mvnrepository.com/artifact/io.quarkus/quarkus-maven-plugin)):

```shell
mvn io.quarkus:quarkus-maven-plugin:1.2.1.Final:create -DprojectGroupId=ibm.gse.eda -DprojectArtifactId=demo -Dextensions="smallrye-health, smallrye-metrics, smallrye-openapi, smallrye-fault-tolerance, smallrye-jwt, resteasy, resteasy-jsonb, arc"
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

## CDI

CDI is supported by ArC and is part of the `quarkus-resteasy` extension. Variable injection is done with `@Inject` and `@ConfigProperties`. The order is properties < System.getEnv()  < System.getProperties()

```java
@Inject 
@ConfigProperty(name = "main.topic.name")
protected String mainTopicName;
```

Bean classes that don’t have a bean defining annotation are not discovered. This behavior is defined by CDI. But producer methods and fields and observer methods are discovered even if the declaring class is not annotated with a bean defining annotation

By default, CDI beans are created lazily, when needed.

* A normal scoped bean (@ApplicationScoped, @RequestScoped, etc.) is needed when a method is invoked upon an injected instance (contextual reference per the specification).
* A bean with a pseudo-scope (@Dependent and @Singleton ) is created when injected

In CDI event Singleton injection may not work as expected due to the propagation of the application context. For example in the case where we need to run the kafka consumer in an executor, we need to pass the configuration to the runner so it get the value injected in the properties, as those value are created in the application context, and will be null in the thread context. 

The configuration is injected into the runner in the application Bean


```java
@ApplicationScoped
public class ApplicationBean {
    @Inject
    public KafkaConfiguration kafkaConfiguration;
     
    public void onStart(@Observes StartupEvent ev) {
        executorService = Executors.newFixedThreadPool(1);
        mainEventRunner = new MainEventsRunner(kafkaConfiguration);

```

```java
@ApplicationScoped
public class MainEventsRunner implements Runnable {}
    public KafkaConfiguration kafkaConfiguration;
    
	public MainEventsRunner(KafkaConfiguration kafkaConfiguration) {
		this.kafkaConfiguration = kafkaConfiguration;
    }
```

## Quick list of common commands

See also this [Cheat sheer](https://lordofthejars.github.io/quarkus-cheat-sheet/#quarkuscheatsheet)

## Reading or viewing

* [Visual Studio Quarkus extension video](https://www.youtube.com/watch?v=S3X2GMWcf_Q&pbjreload=10)
* [QUARKUS - TIPS FOR WRITING NATIVE APPLICATIONS](https://quarkus.io/guides/writing-native-applications-tips)
