package com.oreilly.rxjava.ch7;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.TimeInterval;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.time.Month.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Ignore
public class Chapter7 {

	private static final Logger log = LoggerFactory.getLogger(Chapter7.class);

	@Test
	public void sample_9() throws Exception {
		Observable
				.create(subscriber -> {
					try {
						subscriber.onNext(1 / 0);
					} catch (Exception e) {
						subscriber.onError(e);
					}
				})
				//BROKEN, missing onError() callback
				.subscribe(System.out::println);
	}

	@Test
	public void sample_29() throws Exception {
		Observable
				.create(subscriber -> {
					try {
						subscriber.onNext(1 / 0);
					} catch (Exception e) {
						subscriber.onError(e);
					}
				})
				.subscribe(
						System.out::println,
						throwable -> log.error("That escalated quickly", throwable));

	}

	@Test
	public void sample_45() throws Exception {
		Observable.create(subscriber -> {
			try {
				subscriber.onNext(1 / 0);
			} catch (Exception e) {
				subscriber.onError(e);
			}
		});

		Observable.create(subscriber -> subscriber.onNext(1 / 0));

		Observable.fromCallable(() -> 1 / 0);
	}

	@Test
	public void sample_60() throws Exception {
		Observable
				.just(1, 0)
				.map(x -> 10 / x);

		Observable
				.just("Lorem", null, "ipsum")
				.filter(String::isEmpty);
	}

	@Test
	public void sample_71() throws Exception {
		Observable
				.just(1, 0)
				.flatMap(x -> (x == 0) ?
						Observable.error(new ArithmeticException("Zero :-(")) :
						Observable.just(10 / x)
				);
	}


	private final PrintHouse printHouse = new PrintHouse();

	@Test
	public void sample_81() throws Exception {
		Observable<Person> person = Observable.just(new Person());
		Observable<InsuranceContract> insurance = Observable.just(new InsuranceContract());
		Observable<Health> health = person.flatMap(this::checkHealth);
		Observable<Income> income = person.flatMap(this::determineIncome);
		Observable<Score> score = Observable
				.zip(health, income, (h, i) -> asses(h, i))
				.map(this::translate);
		Observable<Agreement> agreement = Observable.zip(
				insurance,
				score.filter(Score::isHigh),
				this::prepare);
		Observable<TrackingId> mail = agreement
				.filter(Agreement::postalMailRequired)
				.flatMap(this::print)
				.flatMap(printHouse::deliver);
	}

	private Observable<Agreement> print(Agreement agreement) {
		return Observable.just(agreement);
	}

	private Agreement prepare(InsuranceContract contract, Score score) {
		return new Agreement();
	}

	private Score translate(BigInteger score) {
		return new Score();
	}

	private BigInteger asses(Health h, Income i) {
		return BigInteger.ONE;
	}

	private Observable<Income> determineIncome(Person person) {
		return Observable.error(new RuntimeException("Foo"));
	}

	private Observable<Health> checkHealth(Person person) {
		return Observable.just(new Health());
	}

	@Test
	public void sample_129() throws Exception {
		Observable<Person> person = Observable.just(new Person());
		Observable<Income> income = person
				.flatMap(this::determineIncome)
				.onErrorReturn(error -> Income.no());
	}

	public Observable<Income> sample_137() throws Exception {
		Person person = new Person();
		try {
			return determineIncome(person);
		} catch (Exception e) {
			return Observable.just(Income.no());
		}
	}

	@Test
	public void sample_147() throws Exception {
		Observable<Person> person = Observable.just(new Person());
		Observable<Income> income = person
				.flatMap(this::determineIncome)
				.onErrorResumeNext(person.flatMap(this::guessIncome));

	}

	private Observable<Income> guessIncome(Person person) {
		//...
		return Observable.just(new Income(1));
	}

	@Test
	public void sample_161() throws Exception {
		Observable<Person> person = Observable.just(new Person());

		Observable<Income> income = person
				.flatMap(this::determineIncome)
				.flatMap(
						Observable::just,
						th -> Observable.empty(),
						Observable::empty)
				.concatWith(person.flatMap(this::guessIncome))
				.first();
	}

	@Test
	public void sample_175() throws Exception {
		Observable<Person> person = Observable.just(new Person());

		Observable<Income> income = person
				.flatMap(this::determineIncome)
				.flatMap(
						Observable::just,
						th -> person.flatMap(this::guessIncome),
						Observable::empty);
	}

	@Test
	public void sample_187() throws Exception {
		Observable<Person> person = Observable.just(new Person());

		Observable<Income> income = person
				.flatMap(this::determineIncome)
				.onErrorResumeNext(th -> {
					if (th instanceof NullPointerException) {
						return Observable.error(th);
					} else {
						return person.flatMap(this::guessIncome);
					}
				});
	}

	Observable<Confirmation> confirmation() {
		Observable<Confirmation> delayBeforeCompletion =
				Observable
						.<Confirmation>empty()
						.delay(200, MILLISECONDS);
		return Observable
				.just(new Confirmation())
				.delay(100, MILLISECONDS)
				.concatWith(delayBeforeCompletion);
	}

	@Test
	public void sample_215() throws Exception {
		confirmation()
				.timeout(210, MILLISECONDS)
				.forEach(
						System.out::println,
						th -> {
							if ((th instanceof TimeoutException)) {
								System.out.println("Too long");
							} else {
								th.printStackTrace();
							}
						}
				);
	}

	Observable<LocalDate> nextSolarEclipse(LocalDate after) {
		return Observable
				.just(
						LocalDate.of(2016, MARCH, 9),
						LocalDate.of(2016, SEPTEMBER, 1),
						LocalDate.of(2017, FEBRUARY, 26),
						LocalDate.of(2017, AUGUST, 21),
						LocalDate.of(2018, FEBRUARY, 15),
						LocalDate.of(2018, JULY, 13),
						LocalDate.of(2018, AUGUST, 11),
						LocalDate.of(2019, JANUARY, 6),
						LocalDate.of(2019, JULY, 2),
						LocalDate.of(2019, DECEMBER, 26))
				.skipWhile(date -> !date.isAfter(after))
				.zipWith(
						Observable.interval(500, 50, MILLISECONDS),
						(date, x) -> date);
	}

	@Test
	public void sample_253() throws Exception {
		nextSolarEclipse(LocalDate.of(2016, SEPTEMBER, 1))
				.timeout(
						() -> Observable.timer(1000, TimeUnit.MILLISECONDS),
						date -> Observable.timer(100, MILLISECONDS));
	}

	@Test
	public void sample_262() throws Exception {
		Observable<TimeInterval<LocalDate>> intervals =
				nextSolarEclipse(LocalDate.of(2016, JANUARY, 1))
						.timeInterval();
	}

	@Test
	public void sample_271() throws Exception {
		Observable<Instant> timestamps = Observable
				.fromCallable(() -> dbQuery())
				.doOnSubscribe(() -> log.info("subscribe()"))
				.doOnRequest(c -> log.info("Requested {}", c))
				.doOnNext(instant -> log.info("Got: {}", instant));

		timestamps
				.zipWith(timestamps.skip(1), Duration::between)
				.map(Object::toString)
				.subscribe(log::info);
	}

	private Instant dbQuery() {
		return Instant.now();
	}

	@Test
	public void sample_291() throws Exception {
		Observable<String> obs = Observable
				.<String>error(new RuntimeException("Swallowed"))
				.doOnError(th -> log.warn("onError", th))
				.onErrorReturn(th -> "Fallback");
	}

}
