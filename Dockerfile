FROM oracle/graalvm-ce:latest
RUN gu install python native-image
WORKDIR /home

CMD [bash]