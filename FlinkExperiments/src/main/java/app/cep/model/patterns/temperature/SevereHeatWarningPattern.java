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
    /*
    To enable assert checks at runtime, use --ea

    TO: Philipp Wagner
    FROM: DFSklar
    Under normal conditions, each of the patterns comes in with only one LocalWeatherData record.
    I have never encountered a case, when testing with the real-world weather-service data,
    in which more than one is present, thus the asserts below.
    However, I am looking for suggestions from you re: what should be done if multiple instances
    of LocalWeatherData are present for a single "event".
    I have added the asserts only to this particular WarningPattern (severe heat) but
    obviously the decision we make here must be brought over to the other patterns in this app.
    */
    public SevereHeatWarning create(Map<String, List<LocalWeatherData>> pattern) {
        List<LocalWeatherData> first = pattern.get("First Event");
        assert(first.size() == 1);   // See comment above
        List<LocalWeatherData> second = pattern.get("Second Event");
        assert(second.size() == 1);   // See comment above
        return new SevereHeatWarning(first.get(0), second.get(0));
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
