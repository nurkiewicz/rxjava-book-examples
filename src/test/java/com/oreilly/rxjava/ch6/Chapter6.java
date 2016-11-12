package com.oreilly.rxjava.ch6;

import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static rx.Observable.empty;
import static rx.Observable.just;

@Ignore
public class Chapter6 {

	@Test
	public void sample_9() throws Exception {
		long startTime = System.currentTimeMillis();
		Observable
				.interval(7, MILLISECONDS)
				.timestamp()
				.sample(1, SECONDS)
				.map(ts -> ts.getTimestampMillis() - startTime + "ms: " + ts.getValue())
				.take(5)
				.subscribe(System.out::println);
	}

	@Test
	public void sample_25() throws Exception {
		Observable<String> delayedNames = delayedNames();

		delayedNames
				.sample(1, SECONDS)
				.subscribe(System.out::println);
	}

	@Test
	public void sample_37() throws Exception {
		Observable<String> delayedNames = delayedNames();

		delayedNames
				.concatWith(delayedCompletion())
				.sample(1, SECONDS)
				.subscribe(System.out::println);

	}

	private Observable<String> delayedNames() {
		Observable<String> names =
				just("Mary", "Patricia", "Linda",
						"Barbara",
						"Elizabeth", "Jennifer", "Maria", "Susan",
						"Margaret", "Dorothy");

		Observable<Long> absoluteDelayMillis =
				just(0.1, 0.6, 0.9,
						1.1,
						3.3, 3.4, 3.5, 3.6,
						4.4, 4.8)
						.map(d -> (long) (d * 1_000));

		final Observable<String> delayedNames = names
				.zipWith(absoluteDelayMillis,
						(n, d) ->
								just(n)
										.delay(d, MILLISECONDS))
				.flatMap(o -> o);
		return delayedNames;
	}

	static <T> Observable<T> delayedCompletion() {
		return Observable.<T>empty().delay(1, SECONDS);
	}

	@Test
	public void sample_64() throws Exception {
		Observable<Long> obs = Observable.interval(20, MILLISECONDS);

		//equivalent:
		obs.sample(1, SECONDS);
		obs.sample(Observable.interval(1, SECONDS));
	}

	@Test
	public void sample_73() throws Exception {
		Observable<String> delayedNames = delayedNames();

		delayedNames
				.throttleFirst(1, SECONDS)
				.subscribe(System.out::println);
	}

	@Test
	public void sample_93() throws Exception {
		Observable
				.range(1, 7)  //1, 2, 3, ... 7
				.buffer(3)
				.subscribe((List<Integer> list) -> {
							System.out.println(list);
						}
				);
	}

	Repository repository = new SomeRepository();

	@Test
	public void sample_105() throws Exception {
		Observable<Record> events = Observable.range(1, 100).map(x -> new Record());

		events
				.subscribe(repository::store);
//vs.
		events
				.buffer(10)
				.subscribe(repository::storeAll);

	}

	@Test
	public void sample_120() throws Exception {
		Random random = new Random();
		Observable
				.defer(() -> just(random.nextGaussian()))
				.repeat(1000)
				.buffer(100, 1)
				.map(this::averageOfList)
				.subscribe(System.out::println);
	}

	private double averageOfList(List<Double> list) {
		return list
				.stream()
				.collect(Collectors.averagingDouble(x -> x));
	}

	@Test
	public void sample_139() throws Exception {
		Observable<List<Integer>> odd = Observable
				.range(1, 7)
				.buffer(1, 2);
		odd.subscribe(System.out::println);
	}

	@Test
	public void sample_147() throws Exception {
		Observable<Integer> odd = Observable
				.range(1, 7)
				.buffer(1, 2)
				.flatMapIterable(list -> list);
	}

	@Test
	public void sample_155() throws Exception {
		Observable<String> delayedNames = delayedNames();

		delayedNames
				.buffer(1, SECONDS)
				.subscribe(System.out::println);
	}

	@Test
	public void sample_164() throws Exception {
		Observable<KeyEvent> keyEvents = empty();

		Observable<Integer> eventPerSecond = keyEvents
				.buffer(1, SECONDS)
				.map(List::size);
	}

	@Test
	public void sample_173() throws Exception {
		Observable<Duration> insideBusinessHours = Observable
				.interval(1, SECONDS)
				.filter(x -> isBusinessHour())
				.map(x -> Duration.ofMillis(100));
		Observable<Duration> outsideBusinessHours = Observable
				.interval(5, SECONDS)
				.filter(x -> !isBusinessHour())
				.map(x -> Duration.ofMillis(200));

		Observable<Duration> openings = Observable.merge(
				insideBusinessHours, outsideBusinessHours);

		Observable<TeleData> upstream = empty();

		Observable<List<TeleData>> samples = upstream
				.buffer(openings);

		Observable<List<TeleData>> samples2 = upstream
				.buffer(
						openings,
						duration -> empty()
								.delay(duration.toMillis(), MILLISECONDS));

	}

	private static final LocalTime BUSINESS_START = LocalTime.of(9, 0);
	private static final LocalTime BUSINESS_END = LocalTime.of(17, 0);

	private boolean isBusinessHour() {
		ZoneId zone = ZoneId.of("Europe/Warsaw");
		ZonedDateTime zdt = ZonedDateTime.now(zone);
		LocalTime localTime = zdt.toLocalTime();
		return !localTime.isBefore(BUSINESS_START)
				&& !localTime.isAfter(BUSINESS_END);
	}

	@Test
	public void sample_216() throws Exception {
		Observable<KeyEvent> keyEvents = empty();

		Observable<Observable<KeyEvent>> windows = keyEvents.window(1, SECONDS);
		Observable<Integer> eventPerSecond = windows
				.flatMap(eventsInSecond -> eventsInSecond.count());
	}

}
