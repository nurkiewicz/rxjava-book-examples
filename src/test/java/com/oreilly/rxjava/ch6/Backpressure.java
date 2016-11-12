package com.oreilly.rxjava.ch6;

import com.oreilly.rxjava.util.Sleeper;
import org.apache.commons.dbutils.ResultSetIterator;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.observables.SyncOnSubscribe;
import rx.schedulers.Schedulers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

@Ignore
public class Backpressure {

	private static final Logger log = LoggerFactory.getLogger(Backpressure.class);


	private Observable<Dish> dishes() {
		Observable<Dish> dishes = Observable
				.range(1, 1_000_000_000)
				.map(Dish::new);
		return dishes;
	}

	@Test
	public void sample_18() throws Exception {
		Observable
				.range(1, 1_000_000_000)
				.map(Dish::new)
				.subscribe(x -> {
					System.out.println("Washing: " + x);
					sleepMillis(50);
				});
	}

	@Test
	public void sample_32() throws Exception {
		final Observable<Dish> dishes = dishes();

		dishes
				.observeOn(Schedulers.io())
				.subscribe(x -> {
					System.out.println("Washing: " + x);
					sleepMillis(50);
				});

	}

	private void sleepMillis(int millis) {
		Sleeper.sleep(Duration.ofMillis(millis));
	}

	Observable<Integer> myRange(int from, int count) {
		return Observable.create(subscriber -> {
			int i = from;
			while (i < from + count) {
				if (!subscriber.isUnsubscribed()) {
					subscriber.onNext(i++);
				} else {
					return;
				}
			}
			subscriber.onCompleted();
		});
	}

	@Test
	public void sample_65() throws Exception {
		myRange(1, 1_000_000_000)
				.map(Dish::new)
				.observeOn(Schedulers.io())
				.subscribe(x -> {
							System.out.println("Washing: " + x);
							sleepMillis(50);
						},
						Throwable::printStackTrace
				);
	}

	@Test
	public void sample_78() throws Exception {
		Observable
				.range(1, 10)
				.subscribe(new Subscriber<Integer>() {

					@Override
					public void onStart() {
						request(3);
					}

					@Override
					public void onCompleted() {

					}

					@Override
					public void onError(Throwable e) {

					}

					@Override
					public void onNext(Integer integer) {

					}

				});
	}

	@Test
	public void sample_94() throws Exception {
		Observable
				.range(1, 10)
				.subscribe(new Subscriber<Integer>() {

					{
						{
							request(3);
						}
					}

					@Override
					public void onCompleted() {

					}

					@Override
					public void onError(Throwable e) {

					}

					@Override
					public void onNext(Integer integer) {

					}

				});
	}

	@Test
	public void sample_136() throws Exception {
		Observable
				.range(1, 10)
				.subscribe(new Subscriber<Integer>() {

					@Override
					public void onStart() {
						request(1);
					}

					@Override
					public void onCompleted() {

					}

					@Override
					public void onError(Throwable e) {

					}

					@Override
					public void onNext(Integer integer) {
						request(1);
						log.info("Next {}", integer);
					}

					//onCompleted, onError...
				});
	}

	@Test
	public void sample_173() throws Exception {
		myRange(1, 1_000_000_000)
				.map(Dish::new)
				.onBackpressureBuffer()
				//.onBackpressureBuffer(1000, () -> log.warn("Buffer full"))
				//.onBackpressureDrop(dish -> log.warn("Throw away {}", dish))
				.observeOn(Schedulers.io())
				.subscribe(x -> {
					System.out.println("Washing: " + x);
					sleepMillis(50);
				});
	}

	@Test
	public void sample_189() throws Exception {
		Connection connection = null;
		PreparedStatement statement =
				connection.prepareStatement("SELECT ...");
		statement.setFetchSize(1000);
		ResultSet rs = statement.executeQuery();
		Observable<Object[]> result =
				Observable
						.from(ResultSetIterator.iterable(rs))
						.doAfterTerminate(() -> {
							try {
								rs.close();
								statement.close();
								connection.close();
							} catch (SQLException e) {
								log.warn("Unable to close", e);
							}
						});
	}

	@Test
	public void sample_213() throws Exception {
		Observable.OnSubscribe<Double> onSubscribe =
				SyncOnSubscribe.createStateless(
						observer -> observer.onNext(Math.random())
				);

		Observable<Double> rand = Observable.create(onSubscribe);
	}

	@Test
	public void sample_224() throws Exception {
		Observable.OnSubscribe<Long> onSubscribe =
				SyncOnSubscribe.createStateful(
						() -> 0L,
						(cur, observer) -> {
							observer.onNext(cur);
							return cur + 1;
						}
				);

		Observable<Long> naturals = Observable.create(onSubscribe);
	}

	@Test
	public void sample_238() throws Exception {
		Observable<Long> naturals = Observable.create(subscriber -> {
			long cur = 0;
			while (!subscriber.isUnsubscribed()) {
				System.out.println("Produced: " + cur);
				subscriber.onNext(cur++);
			}
		});
	}

	@Test
	public void sample_249() throws Exception {
		ResultSet resultSet = null; //...

		Observable.OnSubscribe<Object[]> onSubscribe = SyncOnSubscribe.createSingleState(
				() -> resultSet,
				(rs, observer) -> {
					try {
						rs.next();
						observer.onNext(toArray(rs));
					} catch (SQLException e) {
						observer.onError(e);
					}
				},
				rs -> {
					try {
						//Also close Statement, Connection, etc.
						rs.close();
					} catch (SQLException e) {
						log.warn("Unable to close", e);
					}
				}
		);

		Observable<Object[]> records = Observable.create(onSubscribe);

	}

	private Object[] toArray(ResultSet rs) {
		//TODO
		return new Object[] {};
	}

	@Test
	public void sample_284() throws Exception {
		Observable<Integer> source = Observable.range(1, 1_000);

		source.subscribe(this::store);

		source
				.flatMap(this::store)
				.subscribe(uuid -> log.debug("Stored: {}", uuid));

		source
				.flatMap(this::store)
				.buffer(100)
				.subscribe(
						hundredUuids -> log.debug("Stored: {}", hundredUuids));
	}

	Observable<Void> store(int x) {
		return Observable.empty();
	}

}
