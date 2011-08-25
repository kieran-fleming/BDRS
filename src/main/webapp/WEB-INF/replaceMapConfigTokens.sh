#!/bin/bash

# This is for doing some text replace in bdrs.map.template.
# pulls out the database details from climatewatch-hibernate-datasource.xml
# and replaces the appropriate tokens. Check bdrs.map afterwards
# to make sure things look ok!

DB_USER=$(xmlstarlet select -N w="http://www.springframework.org/schema/beans" -T -t -m "//w:property[@name='username']" -v @value -n climatewatch-hibernate-datasource.xml)
DB_PASS=$(xmlstarlet select -N w="http://www.springframework.org/schema/beans" -T -t -m "//w:property[@name='password']" -v @value -n climatewatch-hibernate-datasource.xml)
DB_CONNECT=$(xmlstarlet select -N w="http://www.springframework.org/schema/beans" -T -t -m "//w:property[@name='url']" -v @value -n climatewatch-hibernate-datasource.xml)

DB_HOST=$(echo $DB_CONNECT | grep -o //.*: | grep -o '[a-z]*')
DB_NAME=$(echo $DB_CONNECT | egrep -o :[0-9]+/.* | egrep -o /.*$ | grep -o '[a-zA-Z0-9]*')
DB_PORT=$(echo $DB_CONNECT | egrep -o :[0-9]+/ | egrep -o [0-9]+)

echo "Replacement variables:"
echo "user: " $DB_USER
echo "pass: " $DB_PASS
echo "db host: " $DB_HOST
echo "db name: " $DB_NAME
echo "db port: " $DB_PORT

cat bdrs.map.template | sed -e "s/\${database_username}/$DB_USER/" -e "s/\${database_password}/$DB_PASS/" -e "s/\${database_host}/$DB_HOST/" -e "s/\${database_port}/$DB_PORT/" -e "s/\${database_name}/$DB_NAME/" > bdrs.map

