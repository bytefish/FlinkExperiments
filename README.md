# FlinkExperiments #

## Project ##

This project is a sample project for [Apache Flink]. The application parses the [Quality Controlled Local Climatological Data (QCLCD)] of 
March 2015, calculates the maximum daily temperature of the stream by using [Apache Flink] and writes the results back into an [Elasticsearch] 
and [PostgreSQL] database.

## Dataset ##

The data is the [Quality Controlled Local Climatological Data (QCLCD)]: 

> Quality Controlled Local Climatological Data (QCLCD) consist of hourly, daily, and monthly summaries for approximately 
> 1,600 U.S. locations. Daily Summary forms are not available for all stations. Data are available beginning January 1, 2005 
> and continue to the present. Please note, there may be a 48-hour lag in the availability of the most recent data.

The data is available at:

* [http://www.ncdc.noaa.gov/orders/qclcd/](http://www.ncdc.noaa.gov/orders/qclcd/)

## Running the Examples ##

The records in the [Quality Controlled Local Climatological Data (QCLCD)] dataset are not sorted by the timestamp. The dataset needs to be prepared first, so that all records are sorted ascending by the time of measurement.

I have written a small application, that sorts the original CSV data by the measurement time:

* [PrepareWeatherData.java](https://github.com/bytefish/FlinkExperiments/blob/master/FlinkExperiments/src/main/java/csv/sorting/PrepareWeatherData.java)

The result is a sorted CSV file, which can be used to run the examples.

[Apache Flink]: https://flink.apache.org
[Elasticsearch]: https://www.elastic.co/products/elasticsearch 
[PostgreSQL]: https://www.postgresql.org
[Quality Controlled Local Climatological Data (QCLCD)]: https://www.ncdc.noaa.gov/data-access/land-based-station-data/land-based-datasets/quality-controlled-local-climatological-data-qclcd
