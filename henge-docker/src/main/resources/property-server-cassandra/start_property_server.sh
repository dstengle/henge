echo '==================='
echo $cassandra_ip
echo '==================='

cassandra_port=9042
load_file="/tmp/load.cql"
cql_version="3.4.0"
cqlsh -f $load_file $cassandra_ip $cassandra_port --cqlversion=$cql_version

/usr/bin/java -Dspring.profiles.active=dev,cassandra,setmapping -Dcassandra.host=$cassandra_ip -Dcassandra.port=$cassandra_port -jar /tmp/henge.jar
