package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Statistics {
	private double avg;

	private double max;

	private double min;

	private long count;

	public Statistics merge(Statistics newStatistics) {
		Statistics currentStatistics = new Statistics();
		currentStatistics.setCount(count + newStatistics.getCount());
		currentStatistics.setMin(Math.min(min, newStatistics.getMin()));
		currentStatistics.setMax(Math.max(max, newStatistics.getMax()));
		currentStatistics.setAvg((avg + newStatistics.getAvg()) / currentStatistics.getCount());

		return currentStatistics;
	}
}
