package com.example.demo;

import com.example.demo.model.TickData;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class TickController {

	private final TickServiceImpl tickServiceImpl;

	public TickController(TickServiceImpl tickServiceImpl) {
		this.tickServiceImpl = tickServiceImpl;
	}

	@PostMapping("/ticks")
	public ResponseEntity<Void> ticks(@RequestBody TickData tickData, UriComponentsBuilder uriComponentsBuilder) {
		if (tickServiceImpl.isValidTick(tickData)) {
			tickServiceImpl.saveTick(tickData);
			UriComponents uriComponents = uriComponentsBuilder.path("/statistics/{instrument_identifier}").buildAndExpand(tickData.getInstrument());
			return ResponseEntity.created(uriComponents.toUri()).build();
		}
		return ResponseEntity.noContent().build();
	}
}
