// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package stream.sinks.pgsql;

import de.bytefish.pgbulkinsert.IPgBulkInsert;

import java.net.URI;

public class LocalWeatherDataPostgresSink extends BasePostgresSink<pgsql.model.LocalWeatherData> {

    public LocalWeatherDataPostgresSink(URI databaseUri, int bulkSize) {
        super(databaseUri, bulkSize);
    }

    @Override
    protected IPgBulkInsert<pgsql.model.LocalWeatherData> getBulkInsert() {
        return new pgsql.mapping.LocalWeatherDataBulkInsert("sample", "weather_data");
    }

}
