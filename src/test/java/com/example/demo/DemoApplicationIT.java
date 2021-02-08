package com.example.demo;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.example.demo.model.InstrumentTick;
import com.example.demo.model.Statistics;
import org.junit.jupiter.api.BeforeEach;
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

	@Autowired
	private InstrumentTickService instrumentTickService;

	@BeforeEach
	void cleanUp() {
		((InstrumentTickServiceImpl) instrumentTickService).cleanDatabase();
	}

	@Test
	@DisplayName("when two valid instrument ticks are POSTed (successively), then the instrument stats are calculated correctly and fetched")
	void whenValidTicksAreCreatedForAnInstrument_thenStatsAreCorrectlyComputedAndFetched() throws URISyntaxException {
		InstrumentTick instrumentTick = createInstrumentTick("IBM.N", 100, System.currentTimeMillis());

		ResponseEntity<Void> response = doPostRequest(instrumentTick);
		URI location = response.getHeaders().getLocation();
		assertThat(location).isNotNull();
		assertThat(location.toString()).contains("/statistics/" + instrumentTick.getInstrument());
		assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

		Statistics statistics = doGetRequest(instrumentTick.getInstrument());
		assertThat(statistics).isNotNull();
		assertThat(statistics.getCount()).isEqualTo(1);
		assertThat(statistics.getMin()).isEqualTo(instrumentTick.getPrice());
		assertThat(statistics.getMax()).isEqualTo(instrumentTick.getPrice());
		assertThat(statistics.getAvg()).isEqualTo(instrumentTick.getPrice());

		instrumentTick = createInstrumentTick("IBM.N", 200, System.currentTimeMillis());
		response = doPostRequest(instrumentTick);
		location = response.getHeaders().getLocation();
		assertThat(location).isNotNull();
		assertThat(location.toString()).contains("/statistics/" + instrumentTick.getInstrument());
		assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.CREATED.value());

		statistics = doGetRequest(instrumentTick.getInstrument());
		assertThat(statistics).isNotNull();
		assertThat(statistics.getCount()).isEqualTo(2);
		assertThat(statistics.getMin()).isEqualTo(100);
		assertThat(statistics.getMax()).isEqualTo(200);
		assertThat(statistics.getAvg()).isEqualTo(150);
	}

	@Test
	@DisplayName("create multiple POSTs simultaneously for the same instrument with valid data and fetch the stats of the instrument")
	void testConcurrency_singleInstrument() throws InterruptedException, TimeoutException, ExecutionException {
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Set<Callable<HttpStatus>> postCallables = new HashSet<>();
		int postTasks = 10;
		for (int i = 1; i <= postTasks; i++) {
			int finalI = i;
			postCallables.add(() -> {
				InstrumentTick instrumentTick = createInstrumentTick("IBM.N", finalI, System.currentTimeMillis());
				return doPostRequest(instrumentTick).getStatusCode();
			});
		}
		List<Future<HttpStatus>> futures = executorService.invokeAll(postCallables);
		Future<Statistics> statisticsFuture = executorService.submit(() -> doGetRequest("IBM.N"));

		executorService.shutdown();
		boolean termination = executorService.awaitTermination(1, TimeUnit.SECONDS);
		assertThat(termination).isTrue();

		for (Future<HttpStatus> httpStatusFuture : futures) {
			assertThat(httpStatusFuture.get(1, TimeUnit.SECONDS).value()).isEqualTo(HttpStatus.CREATED.value());
		}
		Statistics statistics = statisticsFuture.get(1, TimeUnit.SECONDS);
		assertThat(statistics).isNotNull();
		assertThat(statistics.getCount()).isEqualTo(10);
		assertThat(statistics.getMin()).isEqualTo(1);
		assertThat(statistics.getMax()).isEqualTo(10);
		assertThat(statistics.getAvg()).isEqualTo(5.5);
	}

	@Test
	@DisplayName("POST multiple ticks for multiple instruments with valid data, should compute and fetch stats correctly")
	void testConcurrency_multipleInstruments() throws InterruptedException, TimeoutException, ExecutionException {
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Set<Callable<HttpStatus>> postCallables = new HashSet<>();
		List<String> instruments = List.of("ABC", "DEF", "GHI");
		int postTasks = 3;
		for (String instrument : instruments) {
			for (int i = 1; i <= postTasks; i++) {
				int finalI = i;
				postCallables.add(() -> {
					InstrumentTick instrumentTick = createInstrumentTick(instrument, finalI, System.currentTimeMillis());
					return doPostRequest(instrumentTick).getStatusCode();
				});
			}
		}
		List<Future<HttpStatus>> futures = executorService.invokeAll(postCallables);
		List<Future<Statistics>> futureStatistics = new ArrayList<>();
		for (String instrument : instruments) {
			futureStatistics.add(executorService.submit(() -> doGetRequest(instrument)));
		}
		executorService.shutdown();
		boolean termination = executorService.awaitTermination(1, TimeUnit.SECONDS);
		assertThat(termination).isTrue();

		for (Future<HttpStatus> httpStatusFuture : futures) {
			assertThat(httpStatusFuture.get(1, TimeUnit.SECONDS).value()).isEqualTo(HttpStatus.CREATED.value());
		}
		for (Future<Statistics> statistics : futureStatistics) {
			assertThat(statistics.get()).isNotNull();
			assertThat(statistics.get().getCount()).isEqualTo(3);
			assertThat(statistics.get().getMin()).isEqualTo(1);
			assertThat(statistics.get().getMax()).isEqualTo(3);
			assertThat(statistics.get().getAvg()).isEqualTo(2);
		}
	}

	@Test
	@DisplayName("POST multiple ticks for multiple instruments with invalid data, should compute and fetch stats correctly")
	void testStatistics_multipleInstruments_withInvalidData() throws InterruptedException, ExecutionException {
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Set<Callable<HttpStatus>> postCallables = new HashSet<>();
		List<String> instruments = List.of("ABC", "DEF", "GHI");
		int postTasks = 5;
		for (String instrument : instruments) {
			for (int i = 1; i <= postTasks; i++) {
				int finalI = i;
				postCallables.add(() -> {
					long millis = System.currentTimeMillis();
					// these represent invalid data
					if (finalI == 3 || finalI == 5) {
						millis -= 61_000;
					}
					InstrumentTick instrumentTick = createInstrumentTick(instrument, finalI, millis);
					return doPostRequest(instrumentTick).getStatusCode();
				});
			}
		}
		List<Future<HttpStatus>> futures = executorService.invokeAll(postCallables);
		List<Future<Statistics>> futureStatistics = new ArrayList<>();
		for (String instrument : instruments) {
			futureStatistics.add(executorService.submit(() -> doGetRequest(instrument)));
		}
		executorService.shutdown();
		boolean termination = executorService.awaitTermination(1, TimeUnit.SECONDS);
		assertThat(termination).isTrue();

		int created = 0;
		int noContent = 0;
		for (Future<HttpStatus> future : futures) {
			if (future.get().value() == HttpStatus.CREATED.value()) {
				created++;
			}
			if (future.get().value() == HttpStatus.NO_CONTENT.value()) {
				noContent++;
			}
		}
		assertThat(created).isEqualTo(9);
		assertThat(noContent).isEqualTo(6);

		for (Future<Statistics> statistics : futureStatistics) {
			assertThat(statistics.get()).isNotNull();
			assertThat(statistics.get().getCount()).isEqualTo(3);
			assertThat(statistics.get().getMin()).isEqualTo(1);
			assertThat(statistics.get().getMax()).isEqualTo(4);
			assertThat(statistics.get().getAvg()).isEqualTo(2.33);
		}
	}

	@Test
	@DisplayName("POST multiple ticks for multiple instruments with valid data, should compute and fetch aggregated stats correctly")
	void testAggregatedStats() throws InterruptedException, TimeoutException, ExecutionException {
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Set<Callable<HttpStatus>> postCallables = new HashSet<>();
		List<String> instruments = List.of("ABC", "DEF", "GHI");
		int postTasks = 3;
		for (String instrument : instruments) {
			for (int i = 1; i <= postTasks; i++) {
				int finalI = i;
				postCallables.add(() -> {
					InstrumentTick instrumentTick = createInstrumentTick(instrument, finalI, System.currentTimeMillis());
					return doPostRequest(instrumentTick).getStatusCode();
				});
			}
		}
		List<Future<HttpStatus>> futures = executorService.invokeAll(postCallables);
		Future<Statistics> aggregatedStats = executorService.submit(() -> doGetRequest(""));

		executorService.shutdown();
		boolean termination = executorService.awaitTermination(1, TimeUnit.SECONDS);
		assertThat(termination).isTrue();

		for (Future<HttpStatus> httpStatusFuture : futures) {
			assertThat(httpStatusFuture.get(1, TimeUnit.SECONDS).value()).isEqualTo(HttpStatus.CREATED.value());
		}

		assertThat(aggregatedStats.get()).isNotNull();
		assertThat(aggregatedStats.get().getCount()).isEqualTo(9);
		assertThat(aggregatedStats.get().getMin()).isEqualTo(1);
		assertThat(aggregatedStats.get().getMax()).isEqualTo(3);
		assertThat(aggregatedStats.get().getAvg()).isEqualTo(2);
	}

	private ResponseEntity<Void> doPostRequest(InstrumentTick instrumentTick) throws URISyntaxException {
		HttpHeaders headers = createHeaders();
		HttpEntity<InstrumentTick> httpEntity = createHttpEntity(headers, instrumentTick);
		URI uriPost = createUri("http", "localhost", port, "ticks");
		return restTemplate.postForEntity(uriPost, httpEntity, Void.class);
	}

	private Statistics doGetRequest(String instrument) throws URISyntaxException {
		URI uriGet = createUri("http", "localhost", port, "statistics/" + instrument);
		return restTemplate.getForObject(uriGet, Statistics.class);
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
