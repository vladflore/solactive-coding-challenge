package com.example.demo.model;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TickData {
	private String instrument;

	private double price;

	private Timestamp timestamp;
}
