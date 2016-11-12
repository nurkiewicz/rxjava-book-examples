package com.oreilly.rxjava.ch3;

import rx.Observable;

interface WeatherStation {
    Observable<Temperature> temperature();
    Observable<Wind> wind();
}

class BasicWeatherStation implements WeatherStation {

    @Override
    public Observable<Temperature> temperature() {
        return Observable.just(new Temperature());
    }

    @Override
    public Observable<Wind> wind() {
        return Observable.just(new Wind());
    }
}

class Temperature {}

class Wind {}

