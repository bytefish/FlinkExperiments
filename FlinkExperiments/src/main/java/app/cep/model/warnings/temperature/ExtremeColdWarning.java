// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app.cep.model.warnings.temperature;

import app.cep.model.IWarning;
import model.LocalWeatherData;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.Map;

/**
 * Extreme Cold Warning – Forecast shelter temperature of −50 °F (−46 °C) or colder and air temperature remains below −40 °F (−40 °C) up to the 700 mb (21 inHg) level for
 * three or more consecutive days. Only issued by Weather Forecast Offices in Alaska. Elsewhere, an Extreme Cold Warning can be issued by local Weather Forecast Offices
 * as an experimental product using locally appropriate thresholds, usually with little or no wind.
 */
public class ExtremeColdWarning implements IWarning {

    private final LocalWeatherData localWeatherData0;
    private final LocalWeatherData localWeatherData1;
    private final LocalWeatherData localWeatherData2;

    public ExtremeColdWarning(LocalWeatherData localWeatherData0, LocalWeatherData localWeatherData1, LocalWeatherData localWeatherData2) {
        this.localWeatherData0 = localWeatherData0;
        this.localWeatherData1 = localWeatherData1;
        this.localWeatherData2 = localWeatherData2;
    }

    public LocalWeatherData getLocalWeatherData0() {
        return localWeatherData0;
    }

    public LocalWeatherData getLocalWeatherData1() {
        return localWeatherData1;
    }

    public LocalWeatherData getLocalWeatherData2() {
        return localWeatherData2;
    }

    @Override
    public String toString() {
        return String.format("ExtremeColdWarning (WBAN = %s, First Measurement = (%s), Second Measurement = (%s))",
                localWeatherData0.getStation().getWban(),
                getEventSummary(localWeatherData0),
                getEventSummary(localWeatherData1));
    }

    private String getEventSummary(LocalWeatherData localWeatherData) {

        return String.format("Date = %s, Time = %s, Temperature = %f",
                localWeatherData.getDate(), localWeatherData.getTime(), localWeatherData.getTemperature());
    }

}
