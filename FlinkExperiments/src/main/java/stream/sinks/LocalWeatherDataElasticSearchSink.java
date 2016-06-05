// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package stream.sinks;

import de.bytefish.elasticutils.mapping.IElasticSearchMapping;

public class LocalWeatherDataElasticSearchSink extends BaseElasticSearchSink<elastic.model.LocalWeatherData> {

    public LocalWeatherDataElasticSearchSink(String host, int port) {
        super(host, port);
    }

    @Override
    protected String getIndexName() {
        return "weather_data";
    }

    @Override
    protected IElasticSearchMapping getMapping() {
        return new elastic.mapping.LocalWeatherDataMapper();
    }

}
