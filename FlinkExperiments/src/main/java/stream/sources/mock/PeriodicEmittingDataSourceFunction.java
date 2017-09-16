// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package stream.sources.mock;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * This SourceFunction allows to emit Events in a certain delay from a predefined Iterable.
 *
 * @param <TEventType> Event Type, which will be emitted
 */
public abstract class PeriodicEmittingDataSourceFunction<TEventType> implements SourceFunction<TEventType> {

    @Override
    public void run(SourceContext<TEventType> sourceContext) throws Exception {

        Observable.zip(
                Observable.fromIterable(iterable()),
                Observable.interval(duration().toMillis(), TimeUnit.MILLISECONDS),
                (obs, timer) -> obs
        ).blockingSubscribe(new Consumer<TEventType>() {
            @Override
            public void accept(TEventType event) throws Exception {
                sourceContext.collect(event);
            }
        });
    }

    @Override
    public void cancel() {

    }

    protected abstract Iterable<TEventType> iterable();

    protected abstract Duration duration();
}
