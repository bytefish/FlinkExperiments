// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app.cep;

import app.cep.model.IWarning;
import app.cep.model.IWarningPattern;
import app.cep.model.patterns.temperature.SevereHeatWarningPattern;
import app.cep.model.warnings.temperature.SevereHeatWarning;
import model.LocalWeatherData;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.typeutils.GenericTypeInfo;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
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

        // Now apply the SevereHeatWarningPattern on the Stream:
        DataStream<SevereHeatWarning> warnings =  toWarningStream(maxTemperaturePerDay, new SevereHeatWarningPattern());

        // Print the warning to the Console for now:
        warnings.print();

       // Finally execute the Stream:
        env.execute("CEP Weather Warning Example");
    }

    // Generic attempt for generating the Stream of Weather Warnings:
    private static <TWarningType extends IWarning> DataStream<TWarningType>  toWarningStream(DataStream<LocalWeatherData> localWeatherDataDataStream, IWarningPattern<LocalWeatherData, TWarningType> warningPattern) {
        PatternStream<LocalWeatherData> tempPatternStream = CEP.pattern(
                localWeatherDataDataStream.keyBy(new KeySelector<LocalWeatherData, String>() {
                    @Override
                    public String getKey(LocalWeatherData localWeatherData) throws Exception {
                        return localWeatherData.getStation().getWban();
                    }
                }),
                warningPattern.getEventPattern());

        DataStream<TWarningType> warnings = tempPatternStream.select(new PatternSelectFunction<LocalWeatherData, TWarningType>() {
            @Override
            public TWarningType select(Map<String, LocalWeatherData> map) throws Exception {
                return warningPattern.create(map);
            }
        }, new GenericTypeInfo<TWarningType>(warningPattern.getWarningTargetType()));

        return warnings;
    }

}