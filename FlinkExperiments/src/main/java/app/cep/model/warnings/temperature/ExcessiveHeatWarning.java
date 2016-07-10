// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app.cep.model.warnings.temperature;

import app.cep.model.IWarning;
import model.LocalWeatherData;

import java.util.Map;

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
        return String.format("ExcessiveHeatWarning (WBAN = %s, First Measurement = (%s), Second Measurement = (%s))",
                localWeatherData0.getStation().getWban(),
                getEventSummary(localWeatherData0),
                getEventSummary(localWeatherData1));
    }

    private String getEventSummary(LocalWeatherData localWeatherData) {

        return String.format("Date = %s, Time = %s, Temperature = %f",
                localWeatherData.getDate(), localWeatherData.getTime(), localWeatherData.getTemperature());
    }
}
