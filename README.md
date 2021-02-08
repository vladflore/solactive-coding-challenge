### Implementation ideas:
* use a concurrent map for storing the data/ticks coming from the POST request
* consider using requests memoization
* call the /ticks POST endpoint asynchronously multiple times
* all endpoints can be called in parallel
* GET endpoints should execute in constant time => caching/memoization? / calculate the statistics (over all the instruments and per instrument) when POSTing?
* sliding time interval = 60 seconds i.e. currentTime-timestamp<=60


* more data validation
* UT saveInstrumentTickStats
* Resource slice test
