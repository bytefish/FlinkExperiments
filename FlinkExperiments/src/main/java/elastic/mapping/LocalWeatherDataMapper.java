// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package elastic.mapping;


import de.bytefish.elasticutils.elasticsearch6.mapping.BaseElasticSearchMapping;
import org.elasticsearch.Version;
import org.elasticsearch.index.mapper.RootObjectMapper;
import org.elasticsearch.index.mapper.DateFieldMapper;
import org.elasticsearch.index.mapper.TextFieldMapper;
import org.elasticsearch.index.mapper.ObjectMapper;
import org.elasticsearch.index.mapper.GeoPointFieldMapper;
import org.elasticsearch.index.mapper.NumberFieldMapper;
import org.elasticsearch.index.mapper.NumberFieldMapper.NumberType;

public class LocalWeatherDataMapper extends BaseElasticSearchMapping {

    private static final String INDEX_TYPE = "document";

    public LocalWeatherDataMapper() {
        super(INDEX_TYPE, Version.V_6_5_1);
    }

    @Override
    protected void configureRootObjectBuilder(RootObjectMapper.Builder builder) {
        builder
                .add(new DateFieldMapper.Builder("dateTime"))
                .add(new NumberFieldMapper.Builder("temperature", NumberType.FLOAT))
                .add(new NumberFieldMapper.Builder("windSpeed", NumberType.FLOAT))
                .add(new NumberFieldMapper.Builder("stationPressure", NumberType.FLOAT))
                .add(new TextFieldMapper.Builder("skyCondition"))
                .add(new ObjectMapper.Builder("station")
                        .add(new TextFieldMapper.Builder("wban"))
                        .add(new TextFieldMapper.Builder("name"))
                        .add(new TextFieldMapper.Builder("state"))
                        .add(new TextFieldMapper.Builder("location"))
                        .add(new GeoPointFieldMapper.Builder("coordinates"))
                        .nested(ObjectMapper.Nested.newNested(true, false)));
    }
}