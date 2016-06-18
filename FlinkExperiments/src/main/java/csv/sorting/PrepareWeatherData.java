// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package csv.sorting;

import csv.model.Station;
import csv.parser.Parsers;
import de.bytefish.jtinycsvparser.utils.StringUtils;
import model.LocalWeatherData;
import org.apache.commons.lang3.tuple.ImmutablePair;
import scala.Int;
import stream.sources.csv.converter.LocalWeatherDataConverter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrepareWeatherData {

    public static void main(String[] args) throws Exception {

        // Path to read the CSV data from:
        final Path csvStationDataFilePath = FileSystems.getDefault().getPath("C:\\Users\\philipp\\Downloads\\csv\\201503station.txt");
        final Path csvLocalWeatherDataFilePath = FileSystems.getDefault().getPath("C:\\Users\\philipp\\Downloads\\csv\\201503hourly.txt");
        final Path csvLocalWeatherDataSortedFilePath = FileSystems.getDefault().getPath("C:\\Users\\philipp\\Downloads\\csv\\201503hourly_sorted.txt");

        Comparator<OffsetDateTime> byMeasurementTime = (e1, e2) -> e1.compareTo(e2);

        // Holds the List of Sorted DateTimes (including ZoneOffset):
        List<Integer> indices = new ArrayList<>();

        // Get the sorted indices from the stream of LocalWeatherData Elements:
        try (Stream<LocalWeatherData> stream = getLocalWeatherData(csvStationDataFilePath, csvLocalWeatherDataFilePath)) {

            AtomicInteger currentIndex = new AtomicInteger(0);

            indices = stream
                    .map(x -> OffsetDateTime.of(x.getDate(), x.getTime(), ZoneOffset.ofHours(x.getStation().getTimeZone())))
                    .map(x -> new ImmutablePair<>(currentIndex.getAndAdd(1), x))
                    .sorted((x, y) -> byMeasurementTime.compare(x.getRight(), y.getRight()))
                    .map(x -> x.getLeft())
                    .collect(Collectors.toList());
        }

        // Now sorts the File by Line Number:
        writeSortedFileByIndices(csvLocalWeatherDataFilePath, indices, csvLocalWeatherDataSortedFilePath);
    }

    private static void writeSortedFileByIndices(Path csvFileIn, List<Integer> indices, Path csvFileOut) {
        try {
            List<String> csvDataList = new ArrayList<>();

            try (Stream<String> lines = Files.lines(csvFileIn, StandardCharsets.US_ASCII))
            {
                csvDataList = lines.collect(Collectors.toList());
            }

            try(BufferedWriter writer = Files.newBufferedWriter(csvFileOut)) {
                for (Integer index : indices) {
                    writer.write(csvDataList.get(index));
                    writer.newLine();
                }
            }

        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Stream<model.LocalWeatherData> getLocalWeatherData(Path csvStationPath, Path csvLocalWeatherDataPath) {

        // A map between the WBAN and Station for faster Lookups:
        final Map<String, Station> stationMap = getStationMap(csvStationPath);

        // Turns the Stream of CSV data into the Elasticsearch representation:
        return getLocalWeatherData(csvLocalWeatherDataPath)
                // Only use Measurements with a Station:
                .filter(x -> stationMap.containsKey(x.getWban()))
                // And turn the Station and LocalWeatherData into the ElasticSearch representation:
                .map(x -> {
                    // First get the matching Station:
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

    private static Stream<csv.model.Station> getStations(Path path) {
        return Parsers.StationParser().readFromFile(path, StandardCharsets.US_ASCII)
                .filter(x -> x.isValid())
                .map(x -> x.getResult());
    }

    private static Map<String, csv.model.Station> getStationMap(Path path) {
        try (Stream<csv.model.Station> stationStream = getStations(path)) {
            return stationStream
                    .collect(Collectors.toMap(csv.model.Station::getWban, x -> x));
        }
    }
}
