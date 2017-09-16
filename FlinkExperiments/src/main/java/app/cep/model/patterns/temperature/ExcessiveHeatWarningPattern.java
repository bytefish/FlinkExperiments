// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app.cep.model.patterns.temperature;

import app.cep.model.IWarningPattern;
import app.cep.model.warnings.temperature.ExcessiveHeatWarning;
import model.LocalWeatherData;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.List;
import java.util.Map;

/**
 * Excessive Heat Warning – Extreme Heat Index (HI) values forecast to meet or exceed locally defined warning criteria for at least two days.
 * Specific criteria varies among local Weather Forecast Offices, due to climate variability and the effect of excessive heat on the local
 * population.
 *
 * Typical HI values are maximum daytime temperatures above 105 to 110 °F (41 to 43 °C) and minimum nighttime temperatures above 75 °F (24 °C).
 */
public class ExcessiveHeatWarningPattern implements IWarningPattern<LocalWeatherData, ExcessiveHeatWarning> {

    public ExcessiveHeatWarningPattern() {}

    @Override
    public ExcessiveHeatWarning create(Map<String, List<LocalWeatherData>> pattern) {
        LocalWeatherData first = pattern.get("First Event").get(0);
        LocalWeatherData second = pattern.get("Second Event").get(0);

        return new ExcessiveHeatWarning(first, second);
    }

    @Override
    public Pattern<LocalWeatherData, ?> getEventPattern() {
        return Pattern
                .<LocalWeatherData>begin("First Event").where(
                        new SimpleCondition<LocalWeatherData>() {
                            @Override
                            public boolean filter(LocalWeatherData event) throws Exception {
                                return event.getTemperature() >= 41.0f;
                            }
                        })
                .next("Second Event").where(
                        new SimpleCondition<LocalWeatherData>() {
                            @Override
                            public boolean filter(LocalWeatherData event) throws Exception {
                                return event.getTemperature() >= 41.0f;
                            }
                        })
                .within(Time.days(2));
    }

    @Override
    public Class<ExcessiveHeatWarning> getWarningTargetType() {
        return ExcessiveHeatWarning.class;
    }
}
