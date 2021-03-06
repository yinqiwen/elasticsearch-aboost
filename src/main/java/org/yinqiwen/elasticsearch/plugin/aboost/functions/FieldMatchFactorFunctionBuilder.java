/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.yinqiwen.elasticsearch.plugin.aboost.functions;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.common.lucene.search.function.ScoreFunction;
import org.elasticsearch.common.xcontent.ToXContent.Params;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.fielddata.IndexNumericFieldData;
import org.elasticsearch.index.fielddata.plain.IndexIndexFieldData;
import org.elasticsearch.index.fielddata.plain.SortedSetDVOrdinalsIndexFieldData;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilder;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

/**
 * Builder to construct {@code field_value_factor} functions for a function
 * score query.
 */
public class FieldMatchFactorFunctionBuilder extends ScoreFunctionBuilder<FieldMatchFactorFunctionBuilder> {
    public static final String NAME = "field_match_factor";
    public static final FieldMatchFactorFunction.Modifier DEFAULT_MODIFIER = FieldMatchFactorFunction.Modifier.NONE;
    public static final float DEFAULT_FACTOR = 1;

    private final String field;
    private final String match;
    private float factor = DEFAULT_FACTOR;
    private Double missing;
    private FieldMatchFactorFunction.Modifier modifier = DEFAULT_MODIFIER;

    public FieldMatchFactorFunctionBuilder(String fieldName, String match) {
        if (fieldName == null || match == null) {
            throw new IllegalArgumentException("field_match_factor: field or match must not be null");
        }
        this.field = fieldName;
        this.match = match;
    }

    /**
     * Read from a stream.
     */
    public FieldMatchFactorFunctionBuilder(StreamInput in) throws IOException {
        super(in);
        field = in.readString();
        match = in.readString();
        factor = in.readFloat();
        missing = in.readOptionalDouble();
        modifier = FieldMatchFactorFunction.Modifier.readFromStream(in);
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeString(field);
        out.writeFloat(factor);
        out.writeOptionalDouble(missing);
        modifier.writeTo(out);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public String fieldName() {
        return this.field;
    }

    public FieldMatchFactorFunctionBuilder factor(float boostFactor) {
        this.factor = boostFactor;
        return this;
    }

    public float factor() {
        return this.factor;
    }

    /**
     * Value used instead of the field value for documents that don't have that field defined.
     */
    public FieldMatchFactorFunctionBuilder missing(double missing) {
        this.missing = missing;
        return this;
    }

    public Double missing() {
        return this.missing;
    }

    public FieldMatchFactorFunctionBuilder modifier(FieldMatchFactorFunction.Modifier modifier) {
        if (modifier == null) {
            throw new IllegalArgumentException("field_match_factor: modifier must not be null");
        }
        this.modifier = modifier;
        return this;
    }

    public FieldMatchFactorFunction.Modifier modifier() {
        return this.modifier;
    }

    @Override
    public void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(getName());
        builder.field("field", field);
        builder.field("factor", factor);
        builder.field("match", match);
        if (missing != null) {
            builder.field("missing", missing);
        }
        builder.field("modifier", modifier.name().toLowerCase(Locale.ROOT));
        builder.endObject();
    }

    @Override
    protected boolean doEquals(FieldMatchFactorFunctionBuilder functionBuilder) {
        return Objects.equals(this.field, functionBuilder.field) &&
        		Objects.equals(this.match, functionBuilder.match) &&
                Objects.equals(this.factor, functionBuilder.factor) &&
                Objects.equals(this.missing, functionBuilder.missing) &&
                Objects.equals(this.modifier, functionBuilder.modifier);
    }

    @Override
    protected int doHashCode() {
        return Objects.hash(this.field, this.factor, this.missing, this.modifier);
    }

    @Override
    protected ScoreFunction doToFunction(QueryShardContext context) {
        MappedFieldType fieldType = context.getMapperService().fullName(field);
        SortedSetDVOrdinalsIndexFieldData fieldData = null;
        if (fieldType == null) {
            if(missing == null) {
                throw new ElasticsearchException("Unable to find a field mapper for field [" + field + "]. No 'missing' value defined.");
            }
        } else {
            fieldData = context.getForField(fieldType);
        }
        return new FieldMatchFactorFunction(field,match, factor, modifier, missing, fieldData);
    }

    public static FieldMatchFactorFunctionBuilder fromXContent(QueryParseContext parseContext)
            throws IOException, ParsingException {
        XContentParser parser = parseContext.parser();
        String currentFieldName = null;
        String field = null;
        String match = null;
        float boostFactor = FieldMatchFactorFunctionBuilder.DEFAULT_FACTOR;
        FieldMatchFactorFunction.Modifier modifier = FieldMatchFactorFunction.Modifier.NONE;
        Double missing = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token.isValue()) {
                if ("field".equals(currentFieldName)) {
                    field = parser.text();
                }else if ("match".equals(currentFieldName)) {
                	match = parser.text();
                } else if ("factor".equals(currentFieldName)) {
                    boostFactor = parser.floatValue();
                } else if ("modifier".equals(currentFieldName)) {
                    modifier = FieldMatchFactorFunction.Modifier.fromString(parser.text());
                } else if ("missing".equals(currentFieldName)) {
                    missing = parser.doubleValue();
                } else {
                    throw new ParsingException(parser.getTokenLocation(), NAME + " query does not support [" + currentFieldName + "]");
                }
            } else if ("factor".equals(currentFieldName)
                    && (token == XContentParser.Token.START_ARRAY || token == XContentParser.Token.START_OBJECT)) {
                throw new ParsingException(parser.getTokenLocation(),
                        "[" + NAME + "] field 'factor' does not support lists or objects");
            }
        }

        if (field == null) {
            throw new ParsingException(parser.getTokenLocation(), "[" + NAME + "] required field 'field' missing");
        }

        FieldMatchFactorFunctionBuilder fieldValueFactorFunctionBuilder = new FieldMatchFactorFunctionBuilder(field,match).factor(boostFactor)
                .modifier(modifier);
        if (missing != null) {
            fieldValueFactorFunctionBuilder.missing(missing);
        }
        return fieldValueFactorFunctionBuilder;
    }
}
