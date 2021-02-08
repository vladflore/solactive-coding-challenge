package com.example.demo;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.example.demo.model.Statistics;
import com.example.demo.model.TickData;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TickServiceImpl implements TickService {

	private static final Statistics ZERO_STATS = new Statistics();

	private static final long SECOND_TO_MILLIS = 1000;

	private final DateTimeService dateTimeService;

	private final Map<String, Statistics> database = new HashMap<>();

	@Value("${slidingTimeInterval}")
	private int slidingTimeInterval;

	public TickServiceImpl(DateTimeService dateTimeService) {
		this.dateTimeService = dateTimeService;
	}

	@Override
	public boolean isValidTick(TickData tickData) {
		Timestamp timestamp = tickData.getTimestamp();
		return timestamp.getTime() >= dateTimeService.getCurrentTimestamp().getTime() - slidingTimeInterval * SECOND_TO_MILLIS;
	}

	@Override
	public void saveTick(TickData tickData) {
		Statistics newStatistics = new Statistics(tickData.getPrice(), tickData.getPrice(), tickData.getPrice(), 1L);
		database.merge(tickData.getInstrument(), newStatistics, Statistics::merge);
	}

	@Override
	public Statistics getTickStats(String instrument) {
		return database.getOrDefault(instrument, ZERO_STATS);
	}
}
