package com.example.demo;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.example.demo.model.InstrumentTick;
import com.example.demo.model.Statistics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InstrumentTickServiceImpl implements InstrumentTickService {

	private static final Statistics ZERO_STATS = new Statistics();

	private static final long SECOND_TO_MILLIS = 1000;

	private final DateTimeService dateTimeService;

	/**
	 * This represents an in-memory "database" holding the current stats for a specific instrument.
	 */
	private final Map<String, Statistics> statsDatabase = new HashMap<>();

	@Value("${slidingTimeInterval}")
	private int slidingTimeInterval;

	public InstrumentTickServiceImpl(DateTimeService dateTimeService) {
		this.dateTimeService = dateTimeService;
	}

	@Override
	public boolean isValidInstrumentTick(InstrumentTick instrumentTick) {
		// TODO validate that the timestamp is not in the future
		Timestamp timestamp = instrumentTick.getTimestamp();
		return timestamp.getTime() >= dateTimeService.getCurrentTimestamp().getTime() - slidingTimeInterval * SECOND_TO_MILLIS;
	}

	@Override
	public void saveInstrumentTickStats(InstrumentTick instrumentTick) {
		// TODO could you do better here?
		Statistics newStatistics = new Statistics(instrumentTick.getPrice(), instrumentTick.getPrice(), instrumentTick.getPrice(), 1L);
		statsDatabase.merge(instrumentTick.getInstrument(), newStatistics, Statistics::merge);
	}

	@Override
	public Statistics getInstrumentStats(String instrumentId) {
		return statsDatabase.getOrDefault(instrumentId, ZERO_STATS);
	}
}
