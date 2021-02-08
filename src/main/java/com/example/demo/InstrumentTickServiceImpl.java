package com.example.demo;

import java.sql.Timestamp;
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
				(oldStats, newStats) -> new Statistics(
						(oldStats.getTotal() + newStats.getTotal()) / (oldStats.getCount() + newStats.getCount()),
						Math.max(oldStats.getMax(), newStats.getMax()),
						Math.min(oldStats.getMin(), newStats.getMin()),
						oldStats.getCount() + newStats.getCount(),
						oldStats.getTotal() + newStats.getTotal()));
	}

	@Override
	public Statistics getInstrumentStats(String instrumentId) {
		return statsDatabase.getOrDefault(instrumentId, ZERO_STATS);
	}

	/**
	 * This method is not part of the API, it is just an utility to clean up the in-memory database called from the IT.
	 */
	public void cleanDatabase() {
		statsDatabase.clear();
	}
}
