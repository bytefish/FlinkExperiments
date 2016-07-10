// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app.cep.model;

import org.apache.flink.cep.pattern.Pattern;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * A Warning Pattern describes the pattern of a Warning, which is triggered by an Event.
 *
 * @param <TEventType> Event Type
 * @param <TWarningType> Warning Type
 */
public interface IWarningPattern<TEventType, TWarningType extends IWarning> extends Serializable {

    /**
     * Implements the mapping between the pattern matching result and the warning.
     *
     * @param pattern Pattern, which has been matched by Apache Flink.
     * @return The warning created from the given match result.
     */
    TWarningType create(Map<String, TEventType> pattern);

    /**
     * Implementes the Apache Flink CEP Event Pattern which triggers a warning.
     *
     * @return The Apache Flink CEP Pattern definition.
     */
    Pattern<TEventType, ?> getEventPattern();

    /**
     * Returns the Warning Class for simplifying reflection.
     *
     * @return Class Type of the Warning.
     */
    Class<TWarningType> getWarningTargetType();

}
