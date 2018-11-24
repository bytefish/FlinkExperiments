// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package stream.sinks.elastic;

import de.bytefish.elasticutils.client.IElasticSearchClient;
import de.bytefish.elasticutils.elasticsearch2.client.ElasticSearchClient;
import de.bytefish.elasticutils.elasticsearch2.client.bulk.configuration.BulkProcessorConfiguration;
import de.bytefish.elasticutils.elasticsearch2.client.bulk.options.BulkProcessingOptions;
import de.bytefish.elasticutils.elasticsearch2.mapping.IElasticSearchMapping;
import de.bytefish.elasticutils.elasticsearch2.utils.ElasticSearchUtils;
import elastic.mapping.LocalWeatherDataMapper;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.indices.IndexAlreadyExistsException;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class BaseElasticSearchSink<TEntity> extends RichSinkFunction<TEntity> {

    private final String host;
    private final int port;
    private final int bulkSize;

    private IElasticSearchClient<TEntity> client;

    public BaseElasticSearchSink(String host, int port, int bulkSize) {
        this.host = host;
        this.port = port;
        this.bulkSize = bulkSize;

        this.client = null;
    }

    @Override
    public void invoke(TEntity entity, SinkFunction.Context context) {
        client.index(entity);
    }

    @Override
    public void open(Configuration parameters) throws Exception {

        // Create the Transport Client:
        TransportClient transportClient = createClient();

        // Create Index:
        createIndexAndMapping(transportClient);

        // Finally create the Client:
        BulkProcessingOptions options = BulkProcessingOptions.builder()
                .setBulkActions(bulkSize)
                .build();

        client = new ElasticSearchClient<>(transportClient, getIndexName(), new LocalWeatherDataMapper(), new BulkProcessorConfiguration(options));
    }

    @Override
    public void close() throws Exception {
        client.awaitClose(10, TimeUnit.SECONDS);
    }

    protected abstract String getIndexName();

    protected abstract IElasticSearchMapping getMapping();

    private TransportClient createClient() throws Exception {

        // Create a new Connection:
        TransportClient client = TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));

        // Ensure we have connected nodes:
        List<DiscoveryNode> nodes = client.connectedNodes();

        if (nodes.isEmpty()) {
            throw new RuntimeException("Client is not connected to any Elasticsearch nodes!");
        }

        return client;
    }

    private void createIndexAndMapping(Client client) {
        // Create the Index and Mappings before indexing the entities:
        try {
            createIndex(client, getIndexName());
            createMapping(client, getIndexName(), getMapping());
        } catch (IndexAlreadyExistsException e) {
            // No need to worry. Someone else has already initialized the Elasticsearch database...
        }
    }

    private void createIndex(Client client, String indexName) {
        if (!ElasticSearchUtils.indexExist(client, indexName).isExists()) {
            ElasticSearchUtils.createIndex(client, indexName);
        }
    }

    private void createMapping(Client client, String indexName, IElasticSearchMapping mapping) {
        if (ElasticSearchUtils.indexExist(client, indexName).isExists()) {
            ElasticSearchUtils.putMapping(client, indexName, mapping);
        }
    }
}