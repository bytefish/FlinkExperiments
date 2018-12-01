// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package stream.sinks.elastic;

import de.bytefish.elasticutils.elasticsearch6.mapping.IElasticSearchMapping;

public class LocalWeatherDataElasticSearchSink extends BaseElasticSearchSink<elastic.model.LocalWeatherData> {

    public LocalWeatherDataElasticSearchSink(String host, int port, int bulkSize) {
        super(host, port, bulkSize);
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
