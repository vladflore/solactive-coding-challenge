package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Statistics {
	double avg;

	double max;

	double min;

	long count;

	@JsonIgnore
	double total;
}
