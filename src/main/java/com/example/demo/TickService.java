package com.example.demo;

import com.example.demo.model.Statistics;
import com.example.demo.model.TickData;

public interface TickService {
	boolean isValidTick(TickData tickData);

	void saveTick(TickData tickData);

	Statistics getTickStats(String instrument);
}
