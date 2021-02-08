package com.example.demo;

import com.example.demo.model.InstrumentTick;
import com.example.demo.model.Statistics;

public interface InstrumentTickService {
	/**
	 * This method checks if the instrument tick is valid
	 * i.e. its timestamp falls in the last <code>slidingTimeInterval</code> seconds.
	 * See, <code>slidingTimeInterval</code> in <code>application.properties</code>
	 *
	 * @param instrumentTick the {@link InstrumentTick}
	 * @return <code>boolean</code> stating the validity of the instrument tick
	 */
	boolean isValidInstrumentTick(InstrumentTick instrumentTick);

	/**
	 * This method saves the {@link Statistics} for the instrument tick.
	 * @param instrumentTick the {@link InstrumentTick} holds the data for updating its statistics, if tick's data is still valid
	 * @see InstrumentTickService#isValidInstrumentTick
	 */
	void saveInstrumentTickStats(InstrumentTick instrumentTick);

	/**
	 * This method retrieves the stats of the given instrument.
	 * @param instrument a {@link String} denoting an instrument
	 * @return the instrument's {@link Statistics} data
	 */
	Statistics getInstrumentStats(String instrument);
}
