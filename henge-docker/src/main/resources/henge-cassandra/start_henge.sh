echo '==================='
echo $cassandra_ip
echo '==================='

# WGET the Henge


cassandra_port=9042
load_file="/tmp/load.cql"
cql_version="3.4.0"
cqlsh -f $load_file cassandra_server $cassandra_port --cqlversion=$cql_version

/usr/bin/java -Dspring.profiles.active=dev,cassandra,setmapping -Dcassandra.host=cassandra_server -Dcassandra.port=$cassandra_port -jar /tmp/henge.jar
