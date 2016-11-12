package com.oreilly.rxjava.ch5;

import rx.Observable;

import java.util.concurrent.CompletableFuture;

interface TravelAgency {
	Flight search(User user, GeoLocation location);

	default CompletableFuture<Flight> searchAsync(User user, GeoLocation location) {
		return CompletableFuture.supplyAsync(() -> search(user, location));
	}

	default Observable<Flight> rxSearch(User user, GeoLocation location) {
		return Observable.fromCallable(() -> search(user, location));
	}

}


class SomeTravelAgency implements TravelAgency {

	@Override
	public Flight search(User user, GeoLocation location) {
		return new Flight();
	}
}