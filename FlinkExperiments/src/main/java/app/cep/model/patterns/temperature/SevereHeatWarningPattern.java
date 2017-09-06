// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app.cep.model.patterns.temperature;

import app.cep.model.IWarningPattern;
import app.cep.model.warnings.temperature.ExcessiveHeatWarning;
import app.cep.model.warnings.temperature.SevereHeatWarning;
import model.LocalWeatherData;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.windowing.time.Time;
import java.util.List;
import java.util.Map;

public class SevereHeatWarningPattern implements IWarningPattern<LocalWeatherData, SevereHeatWarning> {

    public SevereHeatWarningPattern() {}

    @Override
    public SevereHeatWarning create(Map<String, List<LocalWeatherData>> pattern) {
        LocalWeatherData first = pattern.get("First Event").get(0);
        LocalWeatherData second = pattern.get("Second Event").get(0);

        return new SevereHeatWarning(first, second);
    }

    @Override
    public Pattern<LocalWeatherData, ?> getEventPattern() {
        SimpleCondition<LocalWeatherData> checkIsAnomaly =
                new SimpleCondition<LocalWeatherData>() {
                    @Override
                    public boolean filter(LocalWeatherData value) throws Exception {
                        return value.getTemperature() >= 30.0f;
                    }
                };
        return Pattern
                .<LocalWeatherData>begin("First Event")
                .where(checkIsAnomaly)
                .next("Second Event")
                .where(checkIsAnomaly)
                .within(Time.days(2));
    }

    @Override
    public Class<SevereHeatWarning> getWarningTargetType() {
        return SevereHeatWarning.class;
    }
}
