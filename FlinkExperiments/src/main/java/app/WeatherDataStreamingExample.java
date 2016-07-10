// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app;

import model.LocalWeatherData;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import stream.sinks.pgsql.LocalWeatherDataPostgresSink;
import stream.sources.csv.LocalWeatherDataSourceFunction;
import utils.DateUtilities;

import java.net.URI;
import java.time.ZoneOffset;
import java.util.Date;

public class WeatherDataStreamingExample {

    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Use the Measurement Timestamp of the Event:
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        // We are sequentially reading the historic data from a CSV file:
        env.setParallelism(1);

        // Path to read the CSV data from:
        final String csvStationDataFilePath = "C:\\Users\\philipp\\Downloads\\csv\\201503station.txt";
        final String csvLocalWeatherDataFilePath = "C:\\Users\\philipp\\Downloads\\csv\\201503hourly_sorted.txt";

        // Add the CSV Data Source and assign the Measurement Timestamp:
        DataStream<model.LocalWeatherData> localWeatherDataDataStream = env
                .addSource(new LocalWeatherDataSourceFunction(csvStationDataFilePath, csvLocalWeatherDataFilePath))
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<LocalWeatherData>() {
                    @Override
                    public long extractAscendingTimestamp(LocalWeatherData localWeatherData) {
                        Date measurementTime = DateUtilities.from(localWeatherData.getDate(), localWeatherData.getTime(), ZoneOffset.ofHours(0));

                        return measurementTime.getTime();
                    }
                });

        // First build a KeyedStream over the Data with LocalWeather:
        KeyedStream<LocalWeatherData, String> localWeatherDataByStation = localWeatherDataDataStream
                // Filte for Non-Null Temperature Values, because we might have missing data:
                .filter(new FilterFunction<LocalWeatherData>() {
                    @Override
                    public boolean filter(LocalWeatherData localWeatherData) throws Exception {
                        return localWeatherData.getTemperature() != null;
                    }
                })
                // Now create the keyed stream by the Station WBAN identifier:
                .keyBy(new KeySelector<LocalWeatherData, String>() {
                    @Override
                    public String getKey(LocalWeatherData localWeatherData) throws Exception {
                        return localWeatherData.getStation().getWban();
                    }
                });

        // Now take the Maximum Temperature per day from the KeyedStream:
        DataStream<LocalWeatherData> maxTemperaturePerDay =
                localWeatherDataByStation
                        // Use non-overlapping tumbling window with 1 day length:
                        .timeWindow(Time.days(1))
                        // And use the maximum temperature:
                        .maxBy("temperature");

        // Converts the general stream into the Elasticsearch specific representation with JsonAttributes:
        DataStream<elastic.model.LocalWeatherData> elasticDailyMaxTemperature = maxTemperaturePerDay
                .map(new MapFunction<model.LocalWeatherData, elastic.model.LocalWeatherData>() {
                    @Override
                    public elastic.model.LocalWeatherData map(model.LocalWeatherData localWeatherData) throws Exception {
                        return elastic.converter.LocalWeatherDataConverter.convert(localWeatherData);
                    }
                });

        // Converts the general stream into the Postgres-specific representation:
        DataStream<pgsql.model.LocalWeatherData> pgsqlDailyMaxTemperature = maxTemperaturePerDay
                .map(new MapFunction<model.LocalWeatherData, pgsql.model.LocalWeatherData>() {
                    @Override
                    public pgsql.model.LocalWeatherData map(model.LocalWeatherData localWeatherData) throws Exception {
                        return pgsql.converter.LocalWeatherDataConverter.convert(localWeatherData);
                    }
                });


        // Add a new ElasticSearch Sink:
        //elasticDailyMaxTemperature.addSink(new LocalWeatherDataElasticSearchSink("127.0.0.1", 9300, 100));

        // Add a new Postgres Sink:
        pgsqlDailyMaxTemperature.addSink(new LocalWeatherDataPostgresSink(URI.create("postgres://philipp:test_pwd@127.0.0.1:5432/sampledb"), 1000));

        // Finally execute the Stream:
        env.execute("Max Temperature By Day example");
    }
}