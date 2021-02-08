package com.example.demo;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class DateTimeService {
	public Timestamp getCurrentTimestamp() {
		return Timestamp.valueOf(LocalDateTime.now());
	}
}
