// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import stream.sinks.LocalWeatherDataElasticSearchSink;
import stream.sources.LocalWeatherDataSourceFunction;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WeatherDataStreamingExample {

    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Use the Measurement Timestamp of the Event:
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        // Path to read the CSV data from:
        final String csvStationDataFilePath = "C:\\Users\\philipp\\Downloads\\csv\\201503station.txt";
        final String csvLocalWeatherDataFilePath = "C:\\Users\\philipp\\Downloads\\csv\\201503hourly.txt";

        // Add the CSV Data Source and assign the Measurement Timestamp:
        DataStream<elastic.model.LocalWeatherData> localWeatherDataDataStream = env
                .addSource(new LocalWeatherDataSourceFunction(csvStationDataFilePath, csvLocalWeatherDataFilePath))
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<elastic.model.LocalWeatherData>() {
                    @Override
                    public long extractAscendingTimestamp(elastic.model.LocalWeatherData localWeatherData) {
                        Date measurementDate = localWeatherData.dateTime;

                        return measurementDate.getTime();
                    }
                });

        // Now Perform the Analysis for the daily maximum value on the Stream:
        DataStream<elastic.model.LocalWeatherData> dailyMaxTemperature = localWeatherDataDataStream
                // Filte for Non-Null Temperature Values, because we might have missing data:
                .filter(new FilterFunction<elastic.model.LocalWeatherData>() {
                    @Override
                    public boolean filter(elastic.model.LocalWeatherData localWeatherData) throws Exception {
                        return localWeatherData.temperature != null;
                    }
                })
                // Now create the keyed stream by the Station WBAN identifier:
                .keyBy(new KeySelector<elastic.model.LocalWeatherData, String>() {
                    @Override
                    public String getKey(elastic.model.LocalWeatherData localWeatherData) throws Exception {
                        return localWeatherData.station.wban;
                    }
                })
                // Create a Tumbling Window with the values of 1 day:
                .timeWindow(Time.of(1, TimeUnit.DAYS))
                // Use the max Temperature of the day:
                .max("temperature")
                // And perform an Identity map, because we want to write all values of this day to the Database:
                .map(new MapFunction<elastic.model.LocalWeatherData, elastic.model.LocalWeatherData>() {
                    @Override
                    public elastic.model.LocalWeatherData map(elastic.model.LocalWeatherData localWeatherData) throws Exception {
                        return localWeatherData;
                    }
                });

        // Add the new ElasticSearch Sink:
        dailyMaxTemperature.addSink(new LocalWeatherDataElasticSearchSink("127.0.0.1", 9300));

        // Finally execute the Stream:
        env.execute("Max Temperature By Day example");
    }
}