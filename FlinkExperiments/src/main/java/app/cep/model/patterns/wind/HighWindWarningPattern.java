// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app.cep.model.patterns.wind;

import app.cep.model.IWarningPattern;
import app.cep.model.warnings.wind.HighWindWarning;
import model.LocalWeatherData;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;

import java.util.Map;
import java.util.List;

public class HighWindWarningPattern implements IWarningPattern<LocalWeatherData, HighWindWarning> {

    public HighWindWarningPattern() {

    }

    @Override
    public HighWindWarning create(Map<String, List<LocalWeatherData>> pattern) {
        LocalWeatherData first = pattern.get("First Event").get(0);

        return new HighWindWarning(first);
    }

    @Override
    public Pattern<LocalWeatherData, ?> getEventPattern() {
        SimpleCondition<LocalWeatherData> checkIsAnomaly =
                new SimpleCondition<LocalWeatherData>() {
                    @Override
                    public boolean filter(LocalWeatherData value) throws Exception {
                        return value.getWindSpeed() >= 39  && value.getWindSpeed() <= 110;
                    }
                };
        return Pattern
                .<LocalWeatherData>begin("First Event")
                .where(checkIsAnomaly);
    }

    @Override
    public Class<HighWindWarning> getWarningTargetType() {
        return HighWindWarning.class;
    }
}
