// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app.cep.model.patterns.wind;

import app.cep.model.IWarningPattern;
import app.cep.model.warnings.wind.HighWindWarning;
import model.LocalWeatherData;
import org.apache.flink.cep.pattern.Pattern;

import java.util.Map;

public class HighWindWarningPattern implements IWarningPattern<LocalWeatherData, HighWindWarning> {

    public HighWindWarningPattern() {

    }

    @Override
    public HighWindWarning create(Map<String, LocalWeatherData> pattern) {
        LocalWeatherData first = pattern.get("First Event");

        return new HighWindWarning(first);
    }

    @Override
    public Pattern<LocalWeatherData, ?> getEventPattern() {
        return Pattern
                .<LocalWeatherData>begin("First Event")
                .subtype(LocalWeatherData.class)
                .where(evt -> evt.getWindSpeed() >= 8 && evt.getWindSpeed() <= 13);
    }
}
