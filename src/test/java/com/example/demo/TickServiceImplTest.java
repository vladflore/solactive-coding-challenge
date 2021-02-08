package com.example.demo;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;

import com.example.demo.model.TickData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import(TickServiceImpl.class)
@TestPropertySource(properties = { "slidingTimeInterval=60" })
class TickServiceImplTest {

	public static final LocalDate LOCAL_DATE = LocalDate.of(2021, Month.FEBRUARY, 8);

	public static final LocalTime LOCAL_TIME = LocalTime.of(14, 30, 15);

	public static final LocalDateTime LOCAL_DATE_TIME_PAST = LocalDateTime.of(LOCAL_DATE, LOCAL_TIME);

	@MockBean
	private DateTimeService dateTimeService;

	@Autowired
	private TickService tickService;

	@Test
	void testShouldPass() {
		Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.of(LOCAL_DATE, LOCAL_TIME.plusSeconds(15)));
		when(dateTimeService.getCurrentTimestamp()).thenReturn(currentTimestamp);

		TickData tickData = new TickData("IBM.N", 143.82, Timestamp.valueOf(LOCAL_DATE_TIME_PAST));

		boolean validTick = tickService.isValidTick(tickData);

		assertThat(validTick).isTrue();
	}

	@Test
	void testShouldFail() {
		Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.of(LOCAL_DATE, LOCAL_TIME.plusSeconds(61)));
		when(dateTimeService.getCurrentTimestamp()).thenReturn(currentTimestamp);

		TickData tickData = new TickData("IBM.N", 143.82, Timestamp.valueOf(LOCAL_DATE_TIME_PAST));

		boolean validTick = tickService.isValidTick(tickData);

		assertThat(validTick).isFalse();
	}
}
