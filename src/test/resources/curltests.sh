curl -v -X PUT -d '{"key1":"val1","key2":"val2"}'  http://localhost:8080/api/evse/100  -H "Content-type:application/json"
curl -v http://localhost :8080/api/evse/100

for i in {1..5}; do curl -v -X PUT -d @testdoc.json  http://localhost:8080/api/evse/$i  -H "Content-type:application/json" ; done
