package com.example.demo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.example.demo.model.InstrumentTick;
import com.example.demo.model.Statistics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InstrumentTickServiceImpl implements InstrumentTickService {

	private static final Statistics ZERO_STATS = new Statistics(0, 0, 0, 0, 0);

	private static final long SECOND_TO_MILLIS = 1000;

	private static final String AGGREGATED_STATS_KEY = "aggregatedStatsKey";

	private final DateTimeService dateTimeService;

	/**
	 * This represents an in-memory "database" holding the current stats for a specific instrument.
	 */
	private final ConcurrentMap<String, Statistics> statsDatabase = new ConcurrentHashMap<>();

	@Value("${slidingTimeInterval}")
	private int slidingTimeInterval;

	public InstrumentTickServiceImpl(DateTimeService dateTimeService) {
		this.dateTimeService = dateTimeService;
	}

	@Override
	public boolean isValidInstrumentTick(InstrumentTick instrumentTick) {
		Timestamp timestamp = instrumentTick.getTimestamp();
		return timestamp.getTime() >= dateTimeService.getCurrentTimestamp().getTime() - slidingTimeInterval * SECOND_TO_MILLIS;
	}

	@Override
	public void saveInstrumentTickStats(InstrumentTick instrumentTick) {
		Statistics newStatistic = new Statistics(instrumentTick.getPrice(), instrumentTick.getPrice(), instrumentTick.getPrice(), 1, instrumentTick.getPrice());
		statsDatabase.merge(instrumentTick.getInstrument(), newStatistic,
				(oldStats, newStats) -> {
					double avg = round((oldStats.getTotal() + newStats.getTotal()) / (oldStats.getCount() + newStats.getCount()));
					return new Statistics(
							avg,
							Math.max(oldStats.getMax(), newStats.getMax()),
							Math.min(oldStats.getMin(), newStats.getMin()),
							oldStats.getCount() + newStats.getCount(),
							oldStats.getTotal() + newStats.getTotal());
				});
	}

	@Override
	public Statistics getInstrumentStats(String instrumentId) {
		return statsDatabase.getOrDefault(instrumentId, ZERO_STATS);
	}

	@Override
	public Statistics getAggregatedStats() {
		long count = statsDatabase.values().stream().map(Statistics::getCount).mapToLong(Long::longValue).sum();
		OptionalDouble min = statsDatabase.values().stream().map(Statistics::getMin).mapToDouble(Double::doubleValue).min();
		OptionalDouble max = statsDatabase.values().stream().map(Statistics::getMax).mapToDouble(Double::doubleValue).max();
		double avgTotal = statsDatabase.values().stream().map(Statistics::getAvg).mapToDouble(Double::doubleValue).sum();

		return new Statistics(round(avgTotal / statsDatabase.keySet().size()), max.orElse(0), min.orElse(0), count, 0);
	}

	/**
	 * This method is not part of the API, it is just an utility to clean up the in-memory database called from the IT.
	 */
	public void cleanDatabase() {
		statsDatabase.clear();
	}

	/**
	 * Rounds the <code>double</code> value to two decimal places(e.g. 2.3333 -> 2.33)
	 * @param value the value to round to 2 decimal places
	 * @return the double value rounded to 2 decimal places
	 */
	private static double round(double value) {
		BigDecimal bd = new BigDecimal(Double.toString(value));
		bd = bd.setScale(2, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}
