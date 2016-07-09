// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app.cep.model.warnings.temperature;

import app.cep.model.IWarning;
import model.LocalWeatherData;

import java.util.Map;

/**
 * Excessive Heat Warning – Extreme Heat Index (HI) values forecast to meet or exceed locally defined warning criteria for at least two days.
 * Specific criteria varies among local Weather Forecast Offices, due to climate variability and the effect of excessive heat on the local
 * population.
 *
 * Typical HI values are maximum daytime temperatures above 105 to 110 °F (41 to 43 °C) and minimum nighttime temperatures above 75 °F (24 °C).
 */
public class ExcessiveHeatWarning implements IWarning {

    private final LocalWeatherData localWeatherData0;
    private final LocalWeatherData localWeatherData1;

    public ExcessiveHeatWarning(LocalWeatherData localWeatherData0, LocalWeatherData localWeatherData1) {
        this.localWeatherData0 = localWeatherData0;
        this.localWeatherData1 = localWeatherData1;
    }

    public LocalWeatherData getLocalWeatherData0() {
        return localWeatherData0;
    }

    public LocalWeatherData getLocalWeatherData1() {
        return localWeatherData1;
    }

    @Override
    public String toString() {
        return String.format("ExcessiveHeatWarning (First Measurement = (%s), Second Measurement = (%s))",
                getEventSummary(localWeatherData0),
                getEventSummary(localWeatherData1));
    }

    private String getEventSummary(LocalWeatherData localWeatherData) {

        return String.format("Date = %s, Time = %s, Temperature = %d",
                localWeatherData.getDate(), localWeatherData.getTime(), localWeatherData.getTemperature());
    }
}
