package com.oreilly.rxjava.ch7;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.math.BigInteger;

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

	@Test
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

}
