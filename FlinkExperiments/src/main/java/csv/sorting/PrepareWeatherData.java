// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package csv.sorting;

import csv.model.Station;
import csv.parser.Parsers;
import de.bytefish.jtinycsvparser.mapping.CsvMappingResult;
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

        // A map between the WBAN and Station for faster Lookups:
        final Map<String, Station> stationMap = getStationMap(csvStationDataFilePath);

        // Holds the List of Sorted DateTimes (including ZoneOffset):
        List<Integer> indices = new ArrayList<>();

        // Comparator for sorting the File:
        Comparator<OffsetDateTime> byMeasurementTime = (e1, e2) -> e1.compareTo(e2);


        // Get the sorted indices from the stream of LocalWeatherData Elements:
        try (Stream<CsvMappingResult<csv.model.LocalWeatherData>> stream = getLocalWeatherData(csvLocalWeatherDataFilePath)) {

            AtomicInteger currentIndex = new AtomicInteger(1);

            indices = stream
                    // Skip the CSV Header:
                    .skip(1)
                    // Start by enumerating ALL mapping results:
                    .map(x -> new ImmutablePair<>(currentIndex.getAndAdd(1), x))
                    // Then only take those lines, that are actually valid:
                    .filter(x -> x.getRight().isValid())
                    // Now take the parsed entity from the CsvMappingResult:
                    .map(x -> new ImmutablePair<>(x.getLeft(), x.getRight().getResult()))
                    // Take only those measurements, that are also available in the list of stations:
                    .filter(x -> stationMap.containsKey(x.getRight().getWban()))
                    // Get the OffsetDateTime from the LocalWeatherData, which includes the ZoneOffset of the Station:
                    .map(x -> {
                        // Get the matching station:
                        csv.model.Station station = stationMap.get(x.getRight().getWban());
                        // Calculate the OffsetDateTime from the given measurement:
                        OffsetDateTime measurementTime = OffsetDateTime.of(x.getRight().getDate(), x.getRight().getTime(), ZoneOffset.ofHours(station.getTimeZone()));
                        // Build the Immutable pair with the Index again:
                        return new ImmutablePair<>(x.getLeft(), measurementTime);
                    })
                    // Now sort the Measurements by their Timestamp:
                    .sorted((x, y) -> byMeasurementTime.compare(x.getRight(), y.getRight()))
                    // Take only the Index:
                    .map(x -> x.getLeft())
                    // And turn it into a List:
                    .collect(Collectors.toList());
        }

        // Now sorts the File by Line Number:
        writeSortedFileByIndices(csvLocalWeatherDataFilePath, indices, csvLocalWeatherDataSortedFilePath);
    }



    private static void writeSortedFileByIndices(Path csvFileIn, List<Integer> indices, Path csvFileOut) {
        try {
            List<String> csvDataList = new ArrayList<>();

            // Read the CSV data with skipping the first line:
            try (Stream<String> lines = Files.lines(csvFileIn, StandardCharsets.US_ASCII).skip(1))
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

    private static Stream<CsvMappingResult<csv.model.LocalWeatherData>> getLocalWeatherData(Path path) {
        return Parsers.LocalWeatherDataParser().readFromFile(path, StandardCharsets.US_ASCII);
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
