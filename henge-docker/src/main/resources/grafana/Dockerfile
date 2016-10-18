FROM grafana/grafana:3.0.4

RUN apt-get update 
RUN apt-get -y install curl
RUN apt-get -y install netcat

COPY ./grafana.ini /etc/grafana/grafana.ini

COPY ./dashboard.json /var/lib/grafana/dashboards/dashboard.json

RUN mv /run.sh /runold.sh
COPY ./myrun.sh /run.sh

COPY ./create-datasource.sh /create-datasource.sh

RUN chmod +x /*.sh

CMD ["/run.sh"]