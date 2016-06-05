// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package stream.sinks;

import de.bytefish.elasticutils.client.ElasticSearchClient;
import de.bytefish.elasticutils.client.IElasticSearchClient;
import de.bytefish.elasticutils.client.bulk.configuration.BulkProcessorConfiguration;
import de.bytefish.elasticutils.client.bulk.options.BulkProcessingOptions;
import de.bytefish.elasticutils.mapping.BaseElasticSearchMapping;
import de.bytefish.elasticutils.mapping.IElasticSearchMapping;
import de.bytefish.elasticutils.utils.ElasticSearchUtils;
import elastic.mapping.LocalWeatherDataMapper;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public abstract class BaseElasticSearchSink<TEntity> extends RichSinkFunction<TEntity> {

    private String host;
    private int port;

    private IElasticSearchClient<TEntity> client = null;

    public BaseElasticSearchSink(String host, int port) {
        this.host = host;
        this.port = port;
    }


    @Override
    public void invoke(TEntity entity) throws Exception {
        client.index(entity);
    }

    @Override
    public void open(Configuration parameters) throws Exception {

        // Build the Transport Client:
        TransportClient transportClient = TransportClient.builder().build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));

        // Create the Index and Mappings before indexing the entities:
        createIndex(transportClient, getIndexName());
        createMapping(transportClient, getIndexName(), getMapping());

        // Bulk Options for the wrapped TransportClient:
        BulkProcessorConfiguration bulkConfiguration = new BulkProcessorConfiguration(BulkProcessingOptions.builder()
                .setBulkActions(100)
                .build());

        // Finally create the Client:
        client = new ElasticSearchClient<>(transportClient, getIndexName(), new LocalWeatherDataMapper(), bulkConfiguration);
    }

    protected abstract String getIndexName();

    protected abstract IElasticSearchMapping getMapping();

    @Override
    public void close() throws Exception {
        // We give the Indexer 10 Seconds for a graceful close:
        client.awaitClose(10, TimeUnit.SECONDS);
    }

    private void createIndex(Client client, String indexName) {
        if(!ElasticSearchUtils.indexExist(client, indexName).isExists()) {
            ElasticSearchUtils.createIndex(client, indexName);
        }
    }

    private void createMapping(Client client, String indexName, IElasticSearchMapping mapping) {
        if(ElasticSearchUtils.indexExist(client, indexName).isExists()) {
            ElasticSearchUtils.putMapping(client, indexName, mapping);
        }
    }
}
