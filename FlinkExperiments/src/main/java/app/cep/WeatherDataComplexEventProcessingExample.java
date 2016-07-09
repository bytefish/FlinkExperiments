// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app.cep;

import app.cep.model.patterns.temperature.ExcessiveHeatWarningPattern;
import app.cep.model.warnings.temperature.ExcessiveHeatWarning;
import model.LocalWeatherData;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import stream.sources.csv.LocalWeatherDataSourceFunction;
import utils.DateUtilities;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;

public class WeatherDataComplexEventProcessingExample {

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
        DataStream<LocalWeatherData> localWeatherDataDataStream = env
                .addSource(new LocalWeatherDataSourceFunction(csvStationDataFilePath, csvLocalWeatherDataFilePath))
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<LocalWeatherData>() {
                    @Override
                    public long extractAscendingTimestamp(LocalWeatherData localWeatherData) {
                        Date measurementTime = DateUtilities.from(localWeatherData.getDate(), localWeatherData.getTime(), ZoneOffset.ofHours(localWeatherData.getStation().getTimeZone()));

                        return measurementTime.getTime();
                    }
                })
                .filter(new FilterFunction<LocalWeatherData>() {
                    @Override
                    public boolean filter(LocalWeatherData localWeatherData) throws Exception {
                        return localWeatherData.getTemperature() != null;
                    }
                });

        // Example Excessive Heat Warning:
        final ExcessiveHeatWarningPattern pattern = new ExcessiveHeatWarningPattern();

        PatternStream<LocalWeatherData> tempPatternStream = CEP.pattern(
                localWeatherDataDataStream.keyBy(new KeySelector<LocalWeatherData, String>() {
                    @Override
                    public String getKey(LocalWeatherData localWeatherData) throws Exception {
                        return localWeatherData.getStation().getWban();
                    }
                }),
                pattern.getEventPattern());

        DataStream<ExcessiveHeatWarning> warnings = tempPatternStream.select(new PatternSelectFunction<LocalWeatherData, ExcessiveHeatWarning>() {
            @Override
            public ExcessiveHeatWarning select(Map<String, LocalWeatherData> map) throws Exception {
                return pattern.create(map);
            }
        });

        warnings.print();

       // Finally execute the Stream:
        env.execute("CEP Weather Warning Example");
    }
}