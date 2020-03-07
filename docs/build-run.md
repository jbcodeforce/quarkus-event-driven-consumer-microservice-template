# Build and run locally

you can build a Quarkus application either as a standard Java application (using the docker build file: `src/main/docker/Dockerfile.jvm`)or as a native executable using GraalVM (and -Dnative) and the (`src/main/docker/Dockerfile.native`).

## Environment variables used

* KAFKA_BROKERS
* KAFKA_APIKEY
If the topic name is different that the one in the application.properties
* KAFKA_MAIN_TOPIC

When connected to a Kafka cluster using TLS certificate add:

* TRUSTSTORE_ENABLED
* TRUSTSTORE_PATH
* TRUSTSTORE_PWD

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell
./mvnw quarkus:dev
```

When launching a Quarkus app simply using mvn quarkus:dev, the running application is configured to open port 5005 for remote debugging. That means that all you have to do is point your remote debugger to that port and you will be able to debug it in your favorite IDE/lightweight editor.

* Verify the memory usage: `ps -o pid,rss,command -p $(grep -f runner)`

## Packaging and running the application

The application is packageable using `./mvnw package`.
It produces the executable `quarkus-kafka-consumer-1.0.0-SNAPSHOT-runner.jar` file in `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/quarkus-kafka-consumer-1.0.0-SNAPSHOT-runner.jar`.

## Creating and run a native executable

Start the GraalVM docker image to work in a contained development environment: 

```shell
docker run -ti -v $(pwd):/home -p 8080:8080 oracle/graalvm-ce:latest 
```

You can create a native (to your local OS) executable using: `./mvnw package -Pnative`.

Or you can use Docker to build the linux native executable using: 

```shell
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your binary: 

```shell
./target/quarkus-kafka-consumer-1.0.0-SNAPSHOT-runner
```

## Deployment to openshift while developing

### Define the configuration map

### Create project

```shell
oc new-project eda-sandbox --display-name="EDA kafka play with quarkus"
```

### Build from native image

Define a build configuration 
```shell
oc new-build quay.io/redhat/ubi-quarkus-native-runner --binary --name=eda-orders-consumer-native -l app=eda-orders-consumer

oc start-build eda-orders-consumer-native --from-file=target/quarkus-kafka-consumer-1.0.0-SNAPSHOT-runner --follow
```

!!! Note:
        The native image does not work as some of the SaslClient classes from Kafka API does not have a public no-argument constructor

### Build from source

Define a pure java build process
```
oc new-build registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift --binary --name=eda-orders-consumer -l app=eda-orders-consumer

./mvnw clean
oc start-build eda-orders-consumer --from-file=. --follow
```

### Config the app and routes

```shell
# the name of the application needs to match the app tag specified with the new build.
oc new-app eda-orders-consumer
# get the deployment configuration
oc get dc
# Add environment variables:
c set env dc/eda-orders-consumer KAFKA_BROKERS=broker-3-qnprtqnp7hnkssdz.kafka.svc01.us-east.eventstreams.cloud.ibm.com:9093,broker-1-qnprtqnp7hnkssdz.kafka.svc01.us-east.eventstreams.cloud.ibm.com:9093,broker-0-qnprtqnp7hnkssdz.kafka.svc01.us-east.eventstreams.cloud.ibm.com:9093,broker-5-qnprtqnp7hnkssdz.kafka.svc01.us-east.eventstreams.cloud.ibm.com:9093,broker-2-qnprtqnp7hnkssdz.kafka.svc01.us-east.eventstreams.cloud.ibm.com:9093,broker-4-qnprtqnp7hnkssdz.kafka.svc01.us-east.eventstreams.cloud.ibm.com:9093
# Add secret for the api key and reference it for environment variables

# expose the service as route to be accessible to external apps
oc expose service eda-orders-consumer
# get apps URL
oc get routes
# verify the access
curl eda-orders-consumer-jb-sandbox.gse-eda-demos-...-0001.us-east.containers.appdomain.cloud/hello
```

### Next pure java build 

```shell
oc start-build eda-orders-consumer --from-file=. --follow
```