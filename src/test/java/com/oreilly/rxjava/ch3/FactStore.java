package com.oreilly.rxjava.ch3;

import rx.Observable;

interface FactStore {
	Observable<ReservationEvent> observe();
}
