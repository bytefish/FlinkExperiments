// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app.cep.model.patterns.temperature;

import app.cep.model.IWarningPattern;
import app.cep.model.warnings.temperature.ExtremeColdWarning;
import model.LocalWeatherData;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.Map;

/**
 * Extreme Cold Warning – Forecast shelter temperature of −50 °F (−46 °C) or colder and air temperature remains below −40 °F (−40 °C) up to the 700 mb (21 inHg) level for
 * three or more consecutive days. Only issued by Weather Forecast Offices in Alaska. Elsewhere, an Extreme Cold Warning can be issued by local Weather Forecast Offices
 * as an experimental product using locally appropriate thresholds, usually with little or no wind.
 */
public class ExtremeColdWarningPattern implements IWarningPattern<LocalWeatherData, ExtremeColdWarning> {

    public ExtremeColdWarningPattern() {

    }

    @Override
    public ExtremeColdWarning create(Map<String, LocalWeatherData> pattern) {
        LocalWeatherData first = (LocalWeatherData) pattern.get("First Event");
        LocalWeatherData second = (LocalWeatherData) pattern.get("Second Event");
        LocalWeatherData third = (LocalWeatherData) pattern.get("Third Event");

        return new ExtremeColdWarning(first, second, third);
    }

    @Override
    public Pattern<LocalWeatherData, ?> getEventPattern() {
        return Pattern
                .<LocalWeatherData>begin("First Event")
                .subtype(LocalWeatherData.class)
                .where(evt -> evt.getTemperature() <= -46.0f)
                .next("Second Event")
                .subtype(LocalWeatherData.class)
                .where(evt -> evt.getTemperature() <= -46.0f)
                .next("Third Event")
                .subtype(LocalWeatherData.class)
                .where(evt -> evt.getTemperature() <= -46.0f )
                .within(Time.days(3));
    }

    @Override
    public Class<ExtremeColdWarning> getWarningTargetType() {
        return ExtremeColdWarning.class;
    }
}
