// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app.cep.model.warnings.wind;

import app.cep.model.IWarning;
import model.LocalWeatherData;

public class HighWindWarning implements IWarning {

    private final LocalWeatherData localWeatherData;

    public HighWindWarning(LocalWeatherData localWeatherData) {
        this.localWeatherData = localWeatherData;
    }

    public LocalWeatherData getLocalWeatherData() {
        return localWeatherData;
    }

}
