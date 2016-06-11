// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package elastic.converter;

import utils.DateUtilities;

import java.time.ZoneOffset;

public class LocalWeatherDataConverter {

    public static elastic.model.LocalWeatherData convert(model.LocalWeatherData modelLocalWeatherData) {

        elastic.model.LocalWeatherData elasticLocalWeatherData = new elastic.model.LocalWeatherData();

        elasticLocalWeatherData.dateTime = DateUtilities.from(modelLocalWeatherData.getDate(), modelLocalWeatherData.getTime(), ZoneOffset.ofHours(modelLocalWeatherData.getStation().getTimeZone()));
        elasticLocalWeatherData.skyCondition = modelLocalWeatherData.getSkyCondition();
        elasticLocalWeatherData.stationPressure = modelLocalWeatherData.getStationPressure();
        elasticLocalWeatherData.temperature = modelLocalWeatherData.getTemperature();
        elasticLocalWeatherData.windSpeed = modelLocalWeatherData.getWindSpeed();

        // Convert the Station data:
        elasticLocalWeatherData.station = convert(modelLocalWeatherData.getStation());

        return elasticLocalWeatherData;
    }

    private static elastic.model.Station convert(model.Station modelStation) {
        elastic.model.Station elasticStation = new elastic.model.Station();

        elasticStation.wban = modelStation.getWban();
        elasticStation.name = modelStation.getName();
        elasticStation.state = modelStation.getState();
        elasticStation.location = modelStation.getLocation();

        // Convert the GeoLocation:
        elasticStation.geoLocation = convert(modelStation.getGeoLocation());

        return elasticStation;
    }

    private static elastic.model.GeoLocation convert(model.GeoLocation modelGeoLocation) {
        elastic.model.GeoLocation elasticGeoLocation = new elastic.model.GeoLocation();

        elasticGeoLocation.lat = modelGeoLocation.getLat();
        elasticGeoLocation.lon = modelGeoLocation.getLon();

        return elasticGeoLocation;
    }

}
