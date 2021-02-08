package com.example.demo;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.stream.Stream;

import com.example.demo.model.InstrumentTick;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import(InstrumentTickServiceImpl.class)
@TestPropertySource(properties = { "slidingTimeInterval=60" })
class InstrumentTickServiceImplTest {

	private static final LocalDate DATE = LocalDate.of(2021, Month.FEBRUARY, 8);

	private static final LocalTime TIME = LocalTime.of(14, 30, 15);

	private static final LocalDateTime TICK_DATE_TIME = LocalDateTime.of(DATE, TIME);

	@Autowired
	private InstrumentTickService instrumentTickService;

	@MockBean
	private DateTimeService dateTimeService;

	private static Stream<Arguments> testData() {
		return Stream.of(
				Arguments.arguments(0, true),
				Arguments.arguments(15, true),
				Arguments.of(30, true),
				Arguments.of(45, true),
				Arguments.of(60, true),
				Arguments.of(75, false)
		);
	}

	@ParameterizedTest(name = "Test #{index}: {0} sec. after tick's timestamp => valid='{1}'")
	@MethodSource("testData")
	void testInstrumentTickValidity(int deltaSeconds, boolean valid) {
		Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.of(DATE, TIME.plusSeconds(deltaSeconds)));
		when(dateTimeService.getCurrentTimestamp()).thenReturn(currentTimestamp);

		InstrumentTick instrumentTick = new InstrumentTick("IBM.N", 143.82, Timestamp.valueOf(TICK_DATE_TIME));

		boolean validTick = instrumentTickService.isValidInstrumentTick(instrumentTick);

		assertThat(validTick).isEqualTo(valid);
	}
}
