// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app.cep.model.patterns.temperature;

import app.cep.model.IWarningPattern;
import app.cep.model.warnings.temperature.ExcessiveHeatWarning;
import app.cep.model.warnings.temperature.SevereHeatWarning;
import model.LocalWeatherData;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.Map;

public class SevereHeatWarningPattern implements IWarningPattern<LocalWeatherData, SevereHeatWarning> {

    public SevereHeatWarningPattern() {}

    @Override
    public SevereHeatWarning create(Map<String, LocalWeatherData> pattern) {
        LocalWeatherData first = pattern.get("First Event");
        LocalWeatherData second = pattern.get("Second Event");

        return new SevereHeatWarning(first, second);
    }

    @Override
    public Pattern<LocalWeatherData, ?> getEventPattern() {
        return Pattern
                .<LocalWeatherData>begin("First Event")
                .where(evt -> evt.getTemperature() >= 30.0f)
                .next("Second Event")
                .where(evt -> evt.getTemperature() >= 30.0f)
                .within(Time.days(2));
    }

    @Override
    public Class<SevereHeatWarning> getWarningTargetType() {
        return SevereHeatWarning.class;
    }
}
