// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app.cep.model.patterns.temperature;

import app.cep.model.IWarningPattern;
import app.cep.model.warnings.temperature.ExtremeColdWarning;
import model.LocalWeatherData;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.Map;
import java.util.List;

/**
 * Extreme Cold Warning – Forecast shelter temperature of −50 °F (−46 °C) or colder and air temperature remains below −40 °F (−40 °C) up to the 700 mb (21 inHg) level for
 * three or more consecutive days. Only issued by Weather Forecast Offices in Alaska. Elsewhere, an Extreme Cold Warning can be issued by local Weather Forecast Offices
 * as an experimental product using locally appropriate thresholds, usually with little or no wind.
 */
public class ExtremeColdWarningPattern implements IWarningPattern<LocalWeatherData, ExtremeColdWarning> {

    public ExtremeColdWarningPattern() {

    }

    @Override
    public ExtremeColdWarning create(Map<String, List<LocalWeatherData>> pattern) {
        LocalWeatherData first = (LocalWeatherData) pattern.get("First Event").get(0);
        LocalWeatherData second = (LocalWeatherData) pattern.get("Second Event").get(0);
        LocalWeatherData third = (LocalWeatherData) pattern.get("Third Event").get(0);

        return new ExtremeColdWarning(first, second, third);
    }

    @Override
    public Pattern<LocalWeatherData, ?> getEventPattern() {
        SimpleCondition<LocalWeatherData> checkIsAnomaly =
                new SimpleCondition<LocalWeatherData>() {
                    @Override
                    public boolean filter(LocalWeatherData value) throws Exception {
                        return value.getTemperature() <= 46.0f;
                    }
                };
        return Pattern
                .<LocalWeatherData>begin("First Event")
                .where(checkIsAnomaly)
                .next("Second Event")
                .where(checkIsAnomaly)
                .next("Third Event")
                .where(checkIsAnomaly)
                .within(Time.days(3));
    }

    @Override
    public Class<ExtremeColdWarning> getWarningTargetType() {
        return ExtremeColdWarning.class;
    }
}
