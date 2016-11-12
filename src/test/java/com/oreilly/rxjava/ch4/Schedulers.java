package com.oreilly.rxjava.ch4;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;

@Ignore
public class Schedulers {

	private static final Logger log = LoggerFactory.getLogger(Schedulers.class);

	ExecutorService poolA = newFixedThreadPool(10, threadFactory("Sched-A-%d"));
	Scheduler schedulerA = rx.schedulers.Schedulers.from(poolA);

	ExecutorService poolB = newFixedThreadPool(10, threadFactory("Sched-B-%d"));
	Scheduler schedulerB = rx.schedulers.Schedulers.from(poolB);

	ExecutorService poolC = newFixedThreadPool(10, threadFactory("Sched-C-%d"));
	Scheduler schedulerC = rx.schedulers.Schedulers.from(poolC);

	private final long start = System.currentTimeMillis();

	void log(Object label) {
		System.out.println(
				System.currentTimeMillis() - start + "\t| " +
						Thread.currentThread().getName()   + "\t| " +
						label);
	}

	private ThreadFactory threadFactory(String pattern) {
		return new ThreadFactoryBuilder()
				.setNameFormat(pattern)
				.build();
	}

	Observable<String> simple() {
		return Observable.create(subscriber -> {
			log("Subscribed");
			subscriber.onNext("A");
			subscriber.onNext("B");
			subscriber.onCompleted();
		});
	}

	@Test
	public void sample_215() throws Exception {
		log("Starting");
		final Observable<String> obs = simple();
		log("Created");
		obs
				.subscribeOn(schedulerA)
				.subscribe(
						x -> log("Got " + x),
						Throwable::printStackTrace,
						() -> log("Completed")
				);
		log("Exiting");
	}


	@Test
	public void sample_33() throws Exception {
		//Don't do this
		Observable<String> obs = Observable.create(subscriber -> {
			log("Subscribed");
			Runnable code = () -> {
				subscriber.onNext("A");
				subscriber.onNext("B");
				subscriber.onCompleted();
			};
			new Thread(code, "Async").start();
		});
	}

	@Test
	public void sample_77() throws Exception {
		log("Starting");
		Observable<String> obs = simple();
		log("Created");
		obs
				.subscribeOn(schedulerA)
				//many other operators
				.subscribeOn(schedulerB)
				.subscribe(
						x -> log("Got " + x),
						Throwable::printStackTrace,
						() -> log("Completed")
				);
		log("Exiting");
	}

	@Test
	public void sample_103() throws Exception {
		log("Starting");
		final Observable<String> obs = simple();
		log("Created");
		obs
				.doOnNext(this::log)
				.map(x -> x + '1')
				.doOnNext(this::log)
				.map(x -> x + '2')
				.subscribeOn(schedulerA)
				.doOnNext(this::log)
				.subscribe(
						x -> log("Got " + x),
						Throwable::printStackTrace,
						() -> log("Completed")
				);
		log("Exiting");
	}

	private final RxGroceries rxGroceries = new RxGroceries();

	@Test
	public void sample_122() throws Exception {
		Observable<BigDecimal> totalPrice = Observable
				.just("bread", "butter", "milk", "tomato", "cheese")
				.subscribeOn(schedulerA)  //BROKEN!!!
				.map(prod -> rxGroceries.doPurchase(prod, 1))
				.reduce(BigDecimal::add)
				.single();
	}

	@Test
	public void sample_135() throws Exception {
		final Observable<BigDecimal> totalPrice = Observable
				.just("bread", "butter", "milk", "tomato", "cheese")
				.subscribeOn(schedulerA)
				.flatMap(prod -> rxGroceries.purchase(prod, 1))
				.reduce(BigDecimal::add)
				.single();
	}

	@Test
	public void sample_145() throws Exception {
		Observable<BigDecimal> totalPrice = Observable
				.just("bread", "butter", "milk", "tomato", "cheese")
				.flatMap(prod ->
						rxGroceries
								.purchase(prod, 1)
								.subscribeOn(schedulerA))
				.reduce(BigDecimal::add)
				.single();
	}

	@Test
	public void sample_157() throws Exception {
		Observable<BigDecimal> totalPrice = Observable
				.just("bread", "butter", "egg", "milk", "tomato",
						"cheese", "tomato", "egg", "egg")
				.groupBy(prod -> prod)
				.flatMap(grouped -> grouped
						.count()
						.map(quantity -> {
							String productName = grouped.getKey();
							return Pair.of(productName, quantity);
						}))
				.flatMap(order -> rxGroceries
						.purchase(order.getKey(), order.getValue())
						.subscribeOn(schedulerA))
				.reduce(BigDecimal::add)
				.single();
	}

	@Test
	public void sample_177() throws Exception {
		log("Starting");
		final Observable<String> obs = simple();
		log("Created");
		obs
				.doOnNext(x -> log("Found 1: " + x))
				.observeOn(schedulerA)
				.doOnNext(x -> log("Found 2: " + x))
				.subscribe(
						x -> log("Got 1: " + x),
						Throwable::printStackTrace,
						() -> log("Completed")
				);
		log("Exiting");
	}

	@Test
	public void sample_194() throws Exception {
		log("Starting");
		final Observable<String> obs = simple();
		log("Created");
		obs
				.doOnNext(x -> log("Found 1: " + x))
				.observeOn(schedulerB)
				.doOnNext(x -> log("Found 2: " + x))
				.observeOn(schedulerC)
				.doOnNext(x -> log("Found 3: " + x))
				.subscribeOn(schedulerA)
				.subscribe(
						x -> log("Got 1: " + x),
						Throwable::printStackTrace,
						() -> log("Completed")
				);
		log("Exiting");
	}

	@Test
	public void sample_214() throws Exception {
		log("Starting");
		Observable<String> obs = Observable.create(subscriber -> {
			log("Subscribed");
			subscriber.onNext("A");
			subscriber.onNext("B");
			subscriber.onNext("C");
			subscriber.onNext("D");
			subscriber.onCompleted();
		});
		log("Created");
		obs
				.subscribeOn(schedulerA)
				.flatMap(record -> store(record).subscribeOn(schedulerB))
				.observeOn(schedulerC)
				.subscribe(
						x -> log("Got: " + x),
						Throwable::printStackTrace,
						() -> log("Completed")
				);
		log("Exiting");
	}

	Observable<UUID> store(String s) {
		return Observable.create(subscriber -> {
			log("Storing " + s);
			//hard work
			subscriber.onNext(UUID.randomUUID());
			subscriber.onCompleted();
		});
	}

	@Test
	public void sample_248() throws Exception {
		Observable
				.just('A', 'B')
				.delay(1, SECONDS, schedulerA)
				.subscribe(this::log);
	}


}
