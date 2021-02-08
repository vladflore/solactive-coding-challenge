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

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	@DisplayName("when two valid instrument ticks are POSTed (successively), then the instrument stats are fetched (GET) and calculated correctly")
	void whenValidTicksAreCreatedForAnInstrument_thenStatsAreCorrectlyFetchedAndComputed() throws URISyntaxException {
		HttpHeaders headers = createHeaders();
		InstrumentTick instrumentTick = createInstrumentTick("IBM.N", 100, System.currentTimeMillis());
		HttpEntity<InstrumentTick> httpEntity = createHttpEntity(headers, instrumentTick);
		URI uriPost = createUri("http", "localhost", port, "ticks");
		ResponseEntity<Void> response = restTemplate.postForEntity(uriPost, httpEntity, Void.class);

		URI location = response.getHeaders().getLocation();
		assertThat(location).isNotNull();
		assertThat(location.toString()).contains("/statistics/" + instrumentTick.getInstrument());
		assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

		URI uriGet = createUri("http", "localhost", port, "statistics/" + instrumentTick.getInstrument());
		Statistics statistics = restTemplate.getForObject(uriGet, Statistics.class);
		assertThat(statistics).isNotNull();
		assertThat(statistics.getAvg()).isEqualTo(instrumentTick.getPrice());
		assertThat(statistics.getCount()).isEqualTo(1);
		assertThat(statistics.getMin()).isEqualTo(instrumentTick.getPrice());
		assertThat(statistics.getMax()).isEqualTo(instrumentTick.getPrice());

		instrumentTick = createInstrumentTick("IBM.N", 200, System.currentTimeMillis());
		httpEntity = createHttpEntity(headers, instrumentTick);
		response = restTemplate.postForEntity(uriPost, httpEntity, Void.class);

		location = response.getHeaders().getLocation();
		assertThat(location).isNotNull();
		assertThat(location.toString()).contains("/statistics/" + instrumentTick.getInstrument());
		assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

		statistics = restTemplate.getForObject(uriGet, Statistics.class);
		assertThat(statistics).isNotNull();
		assertThat(statistics.getAvg()).isEqualTo(150);
		assertThat(statistics.getCount()).isEqualTo(2);
		assertThat(statistics.getMin()).isEqualTo(100);
		assertThat(statistics.getMax()).isEqualTo(200);
	}

	private URI createUri(String protocol, String host, int port, String endpoint) throws URISyntaxException {
		return new URI(protocol + "://" + host + ":" + port + "/" + endpoint);
	}

	private InstrumentTick createInstrumentTick(String instrument, double price, long millis) {
		return new InstrumentTick(instrument, price, new Timestamp(millis));
	}

	private HttpEntity<InstrumentTick> createHttpEntity(HttpHeaders headers, InstrumentTick instrumentTick) {
		return new HttpEntity<>(instrumentTick, headers);
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}
}
