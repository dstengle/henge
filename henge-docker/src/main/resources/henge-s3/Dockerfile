FROM ubuntu:latest
ARG jarPath=henge-service.jar

RUN apt-get update

RUN apt-get -y install wget
RUN apt-get -y install python-setuptools python-dev build-essential

COPY java.sh /tmp/java.sh
RUN sh /tmp/java.sh
COPY $jarPath /tmp/henge.jar
COPY start_henge.sh /tmp/start_henge.sh
ENTRYPOINT ["sh", "/tmp/start_henge.sh"]
