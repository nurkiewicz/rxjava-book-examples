package com.oreilly.rxjava.ch7;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.concurrent.TimeUnit;

@Ignore
public class Monitoring {

	private MetricRegistry metricRegistry;

	@Test
	public void sample_9() throws Exception {
		metricRegistry = new MetricRegistry();
		Slf4jReporter reporter = Slf4jReporter
				.forRegistry(metricRegistry)
				.outputTo(LoggerFactory.getLogger(Monitoring.class))
				.build();
		reporter.start(1, TimeUnit.SECONDS);
	}

	@Test
	public void sample_26() throws Exception {
		final Observable<Integer> observable = Observable.range(1, 100);
		final Counter items = metricRegistry.counter("items");
		observable
				.doOnNext(x -> items.inc())
				.subscribe(/* ... */);
	}

	Observable<Long> makeNetworkCall(long x) {
		//...
		return Observable.just(10L);
	}

	@Test
	public void sample_38() throws Exception {
		final Observable<Integer> observable = Observable.range(1, 100);

		Counter counter = metricRegistry.counter("counter");
		observable
				.doOnNext(x -> counter.inc())
				.flatMap(this::makeNetworkCall)
				.doOnNext(x -> counter.dec())
				.subscribe(/* ... */);
	}

	@Test
	public void sample_55() throws Exception {
		final Observable<Integer> observable = Observable.range(1, 100);
		Counter counter = metricRegistry.counter("counter");

		observable
				.flatMap(x ->
						makeNetworkCall(x)
								.doOnSubscribe(counter::inc)
								.doOnTerminate(counter::dec)
				)
				.subscribe(/* ... */);
	}

	@Test
	public void sample_69() throws Exception {
		Timer timer = metricRegistry.timer("timer");
		Timer.Context ctx = timer.time();
		//some lengthy operation...
		ctx.stop();
	}

	@Test
	public void sample_78() throws Exception {
		Observable<Long> external = Observable.just(42L);

		Timer timer = metricRegistry.timer("timer");

		Observable<Long> externalWithTimer = Observable
				.defer(() -> Observable.just(timer.time()))
				.flatMap(timerCtx ->
						external.doOnCompleted(timerCtx::stop));
	}
}
