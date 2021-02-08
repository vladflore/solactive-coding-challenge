package com.example.demo;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;

import com.example.demo.model.Statistics;
import com.example.demo.model.TickData;
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
	private TickService tickService;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void test() throws URISyntaxException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		TickData tickData = new TickData("IBM.N", 100, new Timestamp(System.currentTimeMillis()));
		HttpEntity<TickData> httpEntity = new HttpEntity<>(tickData, headers);

		URI uri = new URI("http://localhost:" + port + "/ticks");

		ResponseEntity<Void> response = restTemplate.postForEntity(uri, httpEntity, Void.class);

		URI location = response.getHeaders().getLocation();
		assertThat(location).isNotNull();
		assertThat(location.toString()).contains("/statistics/" + tickData.getInstrument());
		assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

		Statistics tickStats = tickService.getTickStats(tickData.getInstrument());
		assertThat(tickStats).isNotNull();
		assertThat(tickStats.getAvg()).isEqualTo(tickData.getPrice());
		assertThat(tickStats.getCount()).isEqualTo(1);
		assertThat(tickStats.getMin()).isEqualTo(tickData.getPrice());
		assertThat(tickStats.getMax()).isEqualTo(tickData.getPrice());

		tickData = new TickData("IBM.N", 200, new Timestamp(System.currentTimeMillis()));
		httpEntity = new HttpEntity<>(tickData, headers);

		response = restTemplate.postForEntity(uri, httpEntity, Void.class);

		location = response.getHeaders().getLocation();
		assertThat(location).isNotNull();
		assertThat(location.toString()).contains("/statistics/" + tickData.getInstrument());
		assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

		tickStats = tickService.getTickStats(tickData.getInstrument());
		assertThat(tickStats).isNotNull();
		assertThat(tickStats.getAvg()).isEqualTo(150);
		assertThat(tickStats.getCount()).isEqualTo(2);

		assertThat(tickStats.getMin()).isEqualTo(100);
		assertThat(tickStats.getMax()).isEqualTo(200);
	}
}
