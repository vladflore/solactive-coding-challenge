# Solactive Code Challenge

### How to run

The project is a Spring Boot Application and as such requires at a minimum:

* Maven (I used version: 3.6.3)
* Java  (I used version: 11.0.8, AdoptOpenJDK)

**Please make sure that the current git branch is `develop`**
```shell
git checkout develop
```

In the `demo` folder execute the following command to compile, test and build the
application:

```shell
mvn clean verify
```

Start the application:

```shell
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

Following endpoints are accessible:

**Please note that the `timestamp` field of the `JSON` payload has to be generated and
used (in these examples instead of `1612817850409`)**

```shell
curl http://localhost:8080/ticks -v -d "{\"instrument\":\"IBM.N\",\"price\":1,\"timestamp\":1612817850409}" --header "Content-Type:application/json"
curl http://localhost:8080/ticks -v -d "{\"instrument\":\"IBM.N\",\"price\":3,\"timestamp\":1612817850409}" --header "Content-Type:application/json"
curl http://localhost:8080/ticks -v -d "{\"instrument\":\"IBM.N\",\"price\":5,\"timestamp\":1612817850409}" --header "Content-Type:application/json"
curl http://localhost:8080/statistics/IBM.N
curl http://localhost:8080/statistics

curl http://localhost:8080/ticks -v -d "{\"instrument\":\"IBM.N1\",\"price\":7,\"timestamp\":1612817850409}" --header "Content-Type:application/json"
curl http://localhost:8080/ticks -v -d "{\"instrument\":\"IBM.N1\",\"price\":9,\"timestamp\":1612817850409}" --header "Content-Type:application/json"
curl http://localhost:8080/ticks -v -d "{\"instrument\":\"IBM.N1\",\"price\":11,\"timestamp\":1612817850409}" --header "Content-Type:application/json"
curl http://localhost:8080/statistics/IBM.N1
curl http://localhost:8080/statistics
```

### Assumptions

Not an assumption, but rather a design decision: calculating the stats on the fly, as the
ticks are coming in, to avoid going over all the ticks when any of the GET requests are
performed (trying to achieve that O(1) complexity). This might lead to some _unwanted
behaviour_ (see point 4 below).

### Improvement possibilities

1. Add (more) data validation
2. Unit Tests for saveInstrumentTickStats and generally more testing
3. Resource slice test
4. Because I do not keep a list of ticks, rather I compute the stats on the fly, as they
   come in, if the last tick came more than 60 seconds ago, that tick should not be part
   of the statistics data anymore (for a specific instrument or for the aggregated stats)
   => bug ?! :(
5. Add logging and exception handling
6. Consider using requests memoization
7. Consider caching the aggregated stats (watch out for inconsistencies between cache data and real data)

### Did I like it?

It was more challenging than I expected, but I enjoyed all those 8+ hours I spent on it! :)
