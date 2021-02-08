package com.example.demo;

import com.example.demo.model.InstrumentTick;
import com.example.demo.model.Statistics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class InstrumentTickResource {

	private final InstrumentTickServiceImpl tickServiceImpl;

	public InstrumentTickResource(InstrumentTickServiceImpl tickServiceImpl) {
		this.tickServiceImpl = tickServiceImpl;
	}

	@PostMapping("/ticks")
	public ResponseEntity<Void> ticks(@RequestBody InstrumentTick instrumentTick, UriComponentsBuilder uriComponentsBuilder) {
		if (tickServiceImpl.isValidInstrumentTick(instrumentTick)) {
			tickServiceImpl.saveInstrumentTickStats(instrumentTick);
			UriComponents uriComponents = uriComponentsBuilder.path("/statistics/{instrument_identifier}").buildAndExpand(instrumentTick.getInstrument());
			return ResponseEntity.created(uriComponents.toUri()).build();
		}
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/statistics/{instrument_identifier}")
	public ResponseEntity<Statistics> getStatsForInstrument(@PathVariable("instrument_identifier") String instrumentId) {
		return ResponseEntity.ok(tickServiceImpl.getInstrumentStats(instrumentId));
	}

	@GetMapping("/statistics")
	public ResponseEntity<Statistics> getAggregatedStats() {
		return ResponseEntity.ok(tickServiceImpl.getAggregatedStats());
	}
}
