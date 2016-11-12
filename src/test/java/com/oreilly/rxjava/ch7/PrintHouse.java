package com.oreilly.rxjava.ch7;

import rx.Observable;

class PrintHouse {

	Observable<TrackingId> deliver(Agreement agreement) {
		return Observable.just(new TrackingId());
	}

}
