package com.oreilly.rxjava.ch5;

import rx.Observable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

class Util {
    static <T> Observable<T> observe(CompletableFuture<T> future) {
        return Observable.create(subscriber -> {
            future.whenComplete((value, exception) -> {
                if (exception != null) {
                    subscriber.onError(exception);
                } else {
                    subscriber.onNext(value);
                    subscriber.onCompleted();
                }
            });
        });
    }

    static <T> CompletableFuture<T> toFuture(Observable<T> observable) {
        CompletableFuture<T> promise = new CompletableFuture<>();
        observable
                .single()
                .subscribe(
                        promise::complete,
                        promise::completeExceptionally
                );
        return promise;
    }

    static <T> CompletableFuture<List<T>> toFutureList(Observable<T> observable) {
        return toFuture(observable.toList());
    }

}
