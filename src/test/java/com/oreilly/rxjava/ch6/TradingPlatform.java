package com.oreilly.rxjava.ch6;

import rx.Observable;

import java.math.BigDecimal;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

class TradingPlatform {

	Observable<BigDecimal> pricesOf(String ticker) {
		return Observable
				.interval(50, MILLISECONDS)
				.flatMap(this::randomDelay)
				.map(this::randomStockPrice)
				.map(BigDecimal::valueOf);
	}

	Observable<Long> randomDelay(long x) {
		return Observable
				.just(x)
				.delay((long) (Math.random() * 100), MILLISECONDS);
	}

	double randomStockPrice(long x) {
		return 100 + Math.random() * 10 +
				(Math.sin(x / 100.0)) * 60.0;
	}


}
