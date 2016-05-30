// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package stream;

import csv.model.LocalWeatherData;
import csv.parser.Parsers;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.stream.Stream;

public class LocalWeatherDataSourceFunction implements SourceFunction<LocalWeatherData> {

    private String filePath;

    public LocalWeatherDataSourceFunction(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void run(SourceFunction.SourceContext<LocalWeatherData> sourceContext) throws Exception {
        final Path path = FileSystems.getDefault().getPath(filePath);

        try(Stream<LocalWeatherData> localWeatherDataStream = getLocalWeatherData(path)) {
            localWeatherDataStream
                    .forEach(sourceContext::collect);
        }
    }

    @Override
    public void cancel() {
    }

    private Stream<csv.model.LocalWeatherData> getLocalWeatherData(Path path) {
        return Parsers.LocalWeatherDataParser().readFromFile(path, StandardCharsets.US_ASCII)
                .filter(x -> x.isValid())
                .map(x -> x.getResult());
    }
}
