package com.example.demo;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;

import com.example.demo.model.InstrumentTick;
import com.example.demo.model.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class DemoApplicationIT {

	@Autowired
	private InstrumentTickService instrumentTickService;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	@DisplayName("when two valid instrument ticks are POSTed, then the instrument stats are calculated correctly")
	void test() throws URISyntaxException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		InstrumentTick instrumentTick = new InstrumentTick("IBM.N", 100, new Timestamp(System.currentTimeMillis()));
		HttpEntity<InstrumentTick> httpEntity = new HttpEntity<>(instrumentTick, headers);

		URI uri = new URI("http://localhost:" + port + "/ticks");

		ResponseEntity<Void> response = restTemplate.postForEntity(uri, httpEntity, Void.class);

		URI location = response.getHeaders().getLocation();
		assertThat(location).isNotNull();
		assertThat(location.toString()).contains("/statistics/" + instrumentTick.getInstrument());
		assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

		Statistics tickStats = instrumentTickService.getInstrumentStats(instrumentTick.getInstrument());
		assertThat(tickStats).isNotNull();
		assertThat(tickStats.getAvg()).isEqualTo(instrumentTick.getPrice());
		assertThat(tickStats.getCount()).isEqualTo(1);
		assertThat(tickStats.getMin()).isEqualTo(instrumentTick.getPrice());
		assertThat(tickStats.getMax()).isEqualTo(instrumentTick.getPrice());

		instrumentTick = new InstrumentTick("IBM.N", 200, new Timestamp(System.currentTimeMillis()));
		httpEntity = new HttpEntity<>(instrumentTick, headers);

		response = restTemplate.postForEntity(uri, httpEntity, Void.class);

		location = response.getHeaders().getLocation();
		assertThat(location).isNotNull();
		assertThat(location.toString()).contains("/statistics/" + instrumentTick.getInstrument());
		assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

		tickStats = instrumentTickService.getInstrumentStats(instrumentTick.getInstrument());
		assertThat(tickStats).isNotNull();
		assertThat(tickStats.getAvg()).isEqualTo(150);
		assertThat(tickStats.getCount()).isEqualTo(2);
		assertThat(tickStats.getMin()).isEqualTo(100);
		assertThat(tickStats.getMax()).isEqualTo(200);
	}
}
