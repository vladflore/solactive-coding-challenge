

* more data validation
* UT saveInstrumentTickStats
* Resource slice test
* because I do not keep a list of ticks, rather I compute the stats on the fly, if the
  last tick came more than 60 seconds ago, that tick should not be part of the statistics
  data anymore (for a specific instrument or for the aggregated stats)
* logging and exception handling
* consider using requests memoization for /statistics to improve performance
* consider caching the aggregated stats
