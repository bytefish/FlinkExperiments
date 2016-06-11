// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package stream.sinks.pgsql;

import de.bytefish.pgbulkinsert.IPgBulkInsert;
import de.bytefish.pgbulkinsert.pgsql.processor.BulkProcessor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import pgsql.connection.PooledConnectionFactory;

import java.net.URI;

public abstract class BasePostgresSink<TEntity> extends RichSinkFunction<TEntity> {

    private final URI databaseUri;
    private final int bulkSize;

    private BulkProcessor<TEntity> bulkProcessor;

    public BasePostgresSink(URI databaseUri, int bulkSize) {
        this.databaseUri = databaseUri;
        this.bulkSize = bulkSize;
    }

    @Override
    public void invoke(TEntity entity) throws Exception {
        bulkProcessor.add(entity);
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        this.bulkProcessor = new BulkProcessor<>(getBulkInsert(), new PooledConnectionFactory(databaseUri), bulkSize);
    }

    @Override
    public void close() throws Exception {
        bulkProcessor.close();
    }

    protected abstract IPgBulkInsert<TEntity> getBulkInsert();
}