// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package stream.sources;

import converter.LocalWeatherDataConverter;
import csv.model.Station;
import csv.parser.Parsers;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalWeatherDataSourceFunction implements SourceFunction<elastic.model.LocalWeatherData> {

    private String stationFilePath;
    private String localWeatherDataFilePath;

    public LocalWeatherDataSourceFunction(String stationFilePath, String localWeatherDataFilePath) {
        this.stationFilePath = stationFilePath;
        this.localWeatherDataFilePath = localWeatherDataFilePath;
    }

    @Override
    public void run(SourceFunction.SourceContext<elastic.model.LocalWeatherData> sourceContext) throws Exception {

        final Path csvStationPath = FileSystems.getDefault().getPath(stationFilePath);
        final Path csvLocalWeatherDataPath = FileSystems.getDefault().getPath(localWeatherDataFilePath);

        try(Stream<elastic.model.LocalWeatherData> localWeatherDataStream = getLocalWeatherData(csvStationPath, csvLocalWeatherDataPath)) {
            localWeatherDataStream
                    .forEach(sourceContext::collect);
        }
    }

    @Override
    public void cancel() {
    }

    private Stream<elastic.model.LocalWeatherData> getLocalWeatherData(Path csvStationPath, Path csvLocalWeatherDataPath) {

        final Map<String, csv.model.Station> stationMap = getStationMap(csvStationPath);

        return getLocalWeatherData(csvLocalWeatherDataPath)
                .filter(x -> stationMap.containsKey(x.getWban()))
                .map(x -> {
                    // Get the matching Station:
                    csv.model.Station station = stationMap.get(x.getWban());
                    // Convert to the Elastic Representation:
                    return LocalWeatherDataConverter.convert(x, station);
                });
    }

    private static Stream<csv.model.LocalWeatherData> getLocalWeatherData(Path path) {
        return Parsers.LocalWeatherDataParser().readFromFile(path, StandardCharsets.US_ASCII)
                .filter(x -> x.isValid())
                .map(x -> x.getResult());
    }

    private Map<String, csv.model.Station> getStationMap(Path path) {
        try (Stream<csv.model.Station> stationStream = getStations(path)) {
            return stationStream
                    .collect(Collectors.toMap(csv.model.Station::getWban, x -> x));
        }
    }

    private static Stream<csv.model.Station> getStations(Path path) {
        return Parsers.StationParser().readFromFile(path, StandardCharsets.US_ASCII)
                .filter(x -> x.isValid())
                .map(x -> x.getResult());
    }
}
