package com.oreilly.rxjava.ch3;

import rx.Observable;

class CassandraFactStore implements FactStore {
	@Override
	public Observable<ReservationEvent> observe() {
		return Observable.just(new ReservationEvent());
	}
}
