// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package app.cep.model;

import org.apache.flink.cep.pattern.Pattern;

import java.io.Serializable;
import java.util.Map;

public interface IWarningPattern<TEventType, TWarningType> extends Serializable {

    TWarningType create(Map<String, TEventType> pattern);

    Pattern<TEventType, ?> getEventPattern();

}
