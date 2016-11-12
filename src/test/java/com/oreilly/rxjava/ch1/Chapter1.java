package com.oreilly.rxjava.ch1;

import com.oreilly.rxjava.util.Sleeper;
import org.junit.Ignore;
import org.junit.Test;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Ignore
public class Chapter1 {

	private static final String SOME_KEY = "FOO";

	@Test
	public void sample_6() throws Exception {
		Observable.create(s -> {
			s.onNext("Hello World!");
			s.onCompleted();
		}).subscribe(hello -> System.out.println(hello));
	}

	@Test
	public void sample_17() throws Exception {
		Map<String, String> cache = new ConcurrentHashMap<>();
		cache.put(SOME_KEY, "123");

		Observable.create(s -> {
			s.onNext(cache.get(SOME_KEY));
			s.onCompleted();
		}).subscribe(value -> System.out.println(value));
	}

	@Test
	public void sample_35() throws Exception {
		// pseudo-code
		Observable.create(s -> {
			String fromCache = getFromCache(SOME_KEY);
			if (fromCache != null) {
				// emit synchronously
				s.onNext(fromCache);
				s.onCompleted();
			} else {
				// fetch asynchronously
				getDataAsynchronously(SOME_KEY)
						.onResponse(v -> {
							putInCache(SOME_KEY, v);
							s.onNext(v);
							s.onCompleted();
						})
						.onFailure(exception -> {
							s.onError(exception);
						});
			}
		}).subscribe(s -> System.out.println(s));

		Sleeper.sleep(Duration.ofSeconds(2));
	}

	private void putInCache(String key, String value) {
		//do nothing
	}

	private Callback getDataAsynchronously(String key) {
		final Callback callback = new Callback();
		new Thread(() -> {
			Sleeper.sleep(Duration.ofSeconds(1));
			callback.getOnResponse().accept(key + ":123");
		}).start();
		return callback;
	}

	private String getFromCache(String key) {
//		return null;
		return key + ":123";
	}

	@Test
	public void sample_81() throws Exception {
		Observable<Integer> o = Observable.create(s -> {
			s.onNext(1);
			s.onNext(2);
			s.onNext(3);
			s.onCompleted();
		});

		o.map(i -> "Number " + i)
				.subscribe(s -> System.out.println(s));
	}

	@Test
	public void sample_94() throws Exception {
		Observable.<Integer>create(s -> {
			//... async subscription and data emission ...
			new Thread(() -> s.onNext(42), "MyThread").start();
		})
				.doOnNext(i -> System.out.println(Thread.currentThread()))
				.filter(i -> i % 2 == 0)
				.map(i -> "Value " + i + " processed on " + Thread.currentThread())
				.subscribe(s -> System.out.println("SOME VALUE =>" + s));
		System.out.println("Will print BEFORE values are emitted because Observable is async");
		Sleeper.sleep(Duration.ofSeconds(1));
	}

	@Test
	public void sample_108() throws Exception {
		Observable.create(s -> {
			new Thread(() -> {
				s.onNext("one");
				s.onNext("two");
				s.onNext("three");
				s.onNext("four");
				s.onCompleted();
			}).start();
		});
	}

	@Test
	public void sample_121() throws Exception {
		// DO NOT DO THIS
		Observable.create(s -> {
			// Thread A
			new Thread(() -> {
				s.onNext("one");
				s.onNext("two");
			}).start();

			// Thread B
			new Thread(() -> {
				s.onNext("three");
				s.onNext("four");
			}).start();

			// ignoring need to emit s.onCompleted() due to race of threads
		});
		// DO NOT DO THIS
	}

	@Test
	public void sample_142() throws Exception {
		Observable<String> a = Observable.create(s -> {
			new Thread(() -> {
				s.onNext("one");
				s.onNext("two");
				s.onCompleted();
			}).start();
		});

		Observable<String> b = Observable.create(s -> {
			new Thread(() -> {
				s.onNext("three");
				s.onNext("four");
				s.onCompleted();
			}).start();
		});

		// this subscribes to a and b concurrently, and merges into a third sequential stream
		Observable<String> c = Observable.merge(a, b);
	}

	@Test
	public void sample_164() throws Exception {
		String args = SOME_KEY;
		Observable<String> someData = Observable.create(s -> {
			getDataFromServerWithCallback(args, data -> {
				s.onNext(data);
				s.onCompleted();
			});
		});

		someData.subscribe(s -> System.out.println("Subscriber 1: " + s));
		someData.subscribe(s -> System.out.println("Subscriber 2: " + s));

		Observable<String> lazyFallback = Observable.just("Fallback");
		someData
				.onErrorResumeNext(lazyFallback)
				.subscribe(s -> System.out.println(s));

	}

	private void getDataFromServerWithCallback(String args, Consumer<String> consumer) {
		consumer.accept("Random: " + Math.random());
	}

	@Test
	public void sample_188() throws Exception {
		// Iterable<String> as Stream<String>
		// that contains 75 strings
		getDataFromLocalMemorySynchronously()
				.skip(10)
				.limit(5)
				.map(s -> s + "_transformed")
				.forEach(System.out::println);
	}

	private Stream<String> getDataFromLocalMemorySynchronously() {
		return IntStream
				.range(0, 100)
				.mapToObj(Integer::toString);
	}

	@Test
	public void sample_205() throws Exception {
		// Observable<String>
// that emits 75 strings
		getDataFromNetworkAsynchronously()
				.skip(10)
				.take(5)
				.map(s -> s + "_transformed")
				.subscribe(System.out::println);
	}

	private Observable<String> getDataFromNetworkAsynchronously() {
		return Observable
				.range(0, 100)
				.map(Object::toString);
	}

	@Test
	public void sample_225() throws Exception {
		CompletableFuture<String> f1 = getDataAsFuture(1);
		CompletableFuture<String> f2 = getDataAsFuture(2);

		CompletableFuture<String> f3 = f1.thenCombine(f2, (x, y) -> {
			return x+y;
		});
	}

	private CompletableFuture<String> getDataAsFuture(int i) {
		return CompletableFuture.completedFuture("Done: " + i + "\n");
	}

	@Test
	public void sample_240() throws Exception {
		Observable<String> o1 = getDataAsObservable(1);
		Observable<String> o2 = getDataAsObservable(2);

		Observable<String> o3 = Observable.zip(o1, o2, (x, y) -> {
			return x+y;
		});
	}

	private Observable<String> getDataAsObservable(int i) {
		return Observable.just("Done: " + i + "\n");
	}

	@Test
	public void sample_254() throws Exception {
		Observable<String> o1 = getDataAsObservable(1);
		Observable<String> o2 = getDataAsObservable(2);

		// o3 is now a stream of o1 and o2 that emits each item without waiting
		Observable<String> o3 = Observable.merge(o1, o2);
	}

	@Test
	public void sample_265() throws Exception {
		// merge a & b into an Observable stream of 2 values
		Observable<String> a_merge_b = getDataA().mergeWith(getDataB());
	}

	public static Single<String> getDataA() {
		return Single.<String> create(o -> {
			o.onSuccess("DataA");
		}).subscribeOn(Schedulers.io());
	}

	@Test
	public void sample_277() throws Exception {
		// Observable<String> o1 = getDataAsObservable(1);
		// Observable<String> o2 = getDataAsObservable(2);

		Single<String> s1 = getDataAsSingle(1);
		Single<String> s2 = getDataAsSingle(2);

		// o3 is now a stream of s1 and s2 that emits each item without waiting
		Observable<String> o3 = Single.merge(s1, s2);
	}

	private Single<String> getDataAsSingle(int i) {
		return Single.just("Done: " + i);
	}

	public static Single<String> getDataB() {
		return Single.just("DataB")
				.subscribeOn(Schedulers.io());
	}

	static Completable writeToDatabase(Object data) {
		return Completable.create(s -> {
			doAsyncWrite(data,
					// callback for successful completion
					() -> s.onCompleted(),
					// callback for failure with Throwable
					error -> s.onError(error));
		});
	}

	static void doAsyncWrite(Object data, Runnable onSuccess, Consumer<Exception> onError) {
		//store data an run asynchronously:
		onSuccess.run();
	}

}

