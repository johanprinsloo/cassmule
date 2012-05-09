# Test Project for a Spray, Akka and Cassandra web service

The project demonstrates mixed scema and schemaless data structures as well as type safe insert and retreival of data:

```/api/evse/{key}```  PUT and GET free form data - get whatever schema you put
```/api/evse/dev/{key}``` Strict Schema enforced - ```{}```

###Run
```
sbt
>container:start
```

###Test

```
curl -v -X PUT -d '{"key1":"val1","key2":"val2"}'  http://localhost:8080/api/evse/100  -H "Content-type:application/json"
curl -v http://localhost:8080/api/evse/100
```

###Dependencies
Cascal need to be cloned, built and installed locally from:

[https://github.com/johanprinsloo/cascal](https://github.com/johanprinsloo/cascal)