// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app.cep.mocks;

import model.GeoLocation;
import model.LocalWeatherData;
import model.Station;
import stream.sources.periodic.PeriodicEmittingDataSourceFunction;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * We build a Mock here with Fake Data. This Mock sends predefined Measurements with a 1 Second Interval and
 * emits them to the SourceContext. This is done, so that we can simulate Measurements and check if the Warnings
 * are correctly generated.
 *
 * SevereHeatWarning (WBAN = PW01, First Measurement = (Date = 2016-07-02, Time = 12:00, Temperature = 33.000000), Second Measurement = (Date = 2016-07-03, Time = 19:00, Temperature = 33.000000))
 */
public class SevereHeatWarningSourceFunction extends PeriodicEmittingDataSourceFunction<LocalWeatherData> {

    @Override
    protected Iterable<LocalWeatherData> iterable() {
        Station station = new Station("PW01", "Philipps Station", "Unknown", "Germany", 1, new GeoLocation(52.5179604,13.3775465));

        List<LocalWeatherData> sample = Arrays.asList(
                new LocalWeatherData(station, LocalDate.of(2016, 7, 1), LocalTime.of(0, 0), 15.0f, 10.0f, 0.0f, "CLR"),
                new LocalWeatherData(station, LocalDate.of(2016, 7, 1), LocalTime.of(1, 0), 15.0f, 10.0f, 0.0f, "CLR"),
                new LocalWeatherData(station, LocalDate.of(2016, 7, 1), LocalTime.of(7, 0), 15.0f, 10.0f, 0.0f, "CLR"),
                new LocalWeatherData(station, LocalDate.of(2016, 7, 2), LocalTime.of(12, 0), 33.0f, 10.0f, 0.0f, "CLR"),
                new LocalWeatherData(station, LocalDate.of(2016, 7, 2), LocalTime.of(14, 0), 15.0f, 10.0f, 0.0f, "CLR"),
                new LocalWeatherData(station, LocalDate.of(2016, 7, 2), LocalTime.of(16, 0), 15.0f, 10.0f, 0.0f, "CLR"),
                new LocalWeatherData(station, LocalDate.of(2016, 7, 3), LocalTime.of(17, 0), 15.0f, 10.0f, 0.0f, "CLR"),
                new LocalWeatherData(station, LocalDate.of(2016, 7, 3), LocalTime.of(18, 0), 15.0f, 10.0f, 0.0f, "CLR"),
                new LocalWeatherData(station, LocalDate.of(2016, 7, 3), LocalTime.of(19, 0), 33.0f, 10.0f, 0.0f, "CLR"));

        return sample;
    }

    @Override
    protected Duration interval() {
        return Duration.ofSeconds(1);
    }
}
