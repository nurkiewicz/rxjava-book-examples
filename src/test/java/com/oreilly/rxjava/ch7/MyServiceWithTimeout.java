package com.oreilly.rxjava.ch7;

import rx.Observable;
import rx.Scheduler;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

class MyServiceWithTimeout implements MyService {

    private final MyService delegate;
    private final Scheduler scheduler;

    MyServiceWithTimeout(MyService d, Scheduler s) {
        this.delegate = d;
        this.scheduler = s;
    }

    @Override
    public Observable<LocalDate> externalCall() {
        return delegate
                .externalCall()
                .timeout(1, TimeUnit.SECONDS,
                     Observable.empty(),
                     scheduler);
    }
}
