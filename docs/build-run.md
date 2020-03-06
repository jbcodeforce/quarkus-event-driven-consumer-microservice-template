# Build and run locally

## Environment variables used

* KAFKA_BROKERS
* KAFKA_APIKEY
* KAFKA_MAIN_TOPIC
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

```
oc new-project eda-sandbox --display-name="EDA kafka play with quarkus"
oc new-build quay.io/redhat/ubi-quarkus-native-runner --binary --name=eda-consumer -l app=eda-consumer
oc start-build eda-consumer --from-file=target/quarkus-kafka-consumer-1.0.0-SNAPSHOT-runner --follow
oc new-app eda-consumer
oc expose service eda-consumer
oc get routes
```