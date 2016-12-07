FROM ubuntu:latest
ARG jarPath=henge-service.jar

RUN apt-get update

RUN apt-get -y install wget
RUN apt-get -y install python-setuptools python-dev build-essential
RUN easy_install pip
RUN pip install cqlsh

#Get the latest henge-service to use with this Docker image
RUN wget -O /tmp/henge-service.jar --content-disposition "http://nexus2.kenzan-devops.com/nexus/service/local/artifact/maven/content?g=com.kenzan.henge&a=henge-service&v=0.9.1-SNAPSHOT&r=snapshots"

COPY java.sh /tmp/java.sh
RUN sh /tmp/java.sh
RUN cp /tmp/$jarPath /tmp/henge.jar
COPY start_property_server.sh /tmp/start_property_server.sh
COPY load.cql /tmp/load.cql
ENTRYPOINT ["sh", "/tmp/start_property_server.sh"]
