// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app.cep.model.patterns.wind;

import app.cep.model.IWarningPattern;
import app.cep.model.warnings.temperature.ExtremeColdWarning;
import app.cep.model.warnings.wind.ExtremeWindWarning;
import model.LocalWeatherData;
import org.apache.flink.cep.pattern.Pattern;

import java.util.Map;

public class ExtremeWindWarningPattern implements IWarningPattern<LocalWeatherData, ExtremeWindWarning> {

    public ExtremeWindWarningPattern() {

    }

    @Override
    public ExtremeWindWarning create(Map<String, LocalWeatherData> pattern) {
        LocalWeatherData first = pattern.get("First Event");

        return new ExtremeWindWarning(first);
    }

    @Override
    public Pattern<LocalWeatherData, ?> getEventPattern() {
        return Pattern
                .<LocalWeatherData>begin("First Event")
                .where(evt -> evt.getWindSpeed() > 110);
    }

    @Override
    public Class<ExtremeWindWarning> getWarningTargetType() {
        return ExtremeWindWarning.class;
    }
}
