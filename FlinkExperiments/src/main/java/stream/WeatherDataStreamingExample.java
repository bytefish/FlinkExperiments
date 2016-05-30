// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package stream;

import csv.model.LocalWeatherData;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import utils.DateUtils;

import java.util.Date;

public class WeatherDataStreamingExample {

    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Use the Timestamp of the Event:
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        // Path to read the data from:
        final String filePath = "C:\\Users\\philipp\\Downloads\\csv\\201503hourly.txt";

        // Deserialize the incoming JSON data:
        DataStream<LocalWeatherData> localWeatherDataDataStream = env
                .addSource(new LocalWeatherDataSourceFunction(filePath))
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<LocalWeatherData>() {
                    @Override
                    public long extractAscendingTimestamp(LocalWeatherData localWeatherData) {
                        Date measurementDate = DateUtils.from(localWeatherData.getDate(), localWeatherData.getTime());

                        return measurementDate.getTime();
                    }
                });

        // Now perform some Streaming analysis:
        DataStream<Tuple2<String, Float>> dailyMaxTemperature = localWeatherDataDataStream
                .filter(new FilterFunction<LocalWeatherData>() {
                    @Override
                    public boolean filter(LocalWeatherData localWeatherData) throws Exception {
                        return localWeatherData.getDryBulbCelsius() != null;
                    }
                })
                .keyBy(new KeySelector<LocalWeatherData, String>() {
                    @Override
                    public String getKey(LocalWeatherData localWeatherData) throws Exception {
                        return localWeatherData.getWban();
                    }
                })
                .window(EventTimeSessionWindows.withGap(Time.days(1)))
                .max("dryBulbCelsius")
                .map(new MapFunction<LocalWeatherData, Tuple2<String, Float>>() {
                    @Override
                    public Tuple2<String, Float> map(LocalWeatherData localWeatherData) throws Exception {
                        return new Tuple2<String, Float>(localWeatherData.getWban(), localWeatherData.getDryBulbCelsius());
                    }
                });

        dailyMaxTemperature.writeAsCsv("flink_data", FileSystem.WriteMode.OVERWRITE);

        env.execute("Max Temperature By Day example");
    }
}
