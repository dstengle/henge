#!/bin/bash -e

# Creates influxdb datasource only once
STAMP="/grafana-datasource-setup-complete"
if [ ! -f ${STAMP} ]; then
	until nc -z localhost 3000; do sleep 1; done
	until nc -z influxdb 8086; do sleep 1; done

	# create influxdb datasource
	curl -H "Content-Type: application/json" -X POST -d '{"name":"HengeMetrics", "type":"influxdb", "url":"http://influxdb:8086", "access":"proxy", "isDefault":true, "database":"henge", "user":"n/a","password":"n/a"}' 'http://admin:admin@localhost:3000/api/datasources'

	touch ${STAMP}
fi