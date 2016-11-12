package com.oreilly.rxjava.ch3;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;
import rx.Subscriber;

import static rx.Observable.just;

@Ignore
public class CustomOperators {

	static <T> Observable<T> odd(Observable<T> upstream) {
		Observable<Boolean> trueFalse = just(true, false).repeat();
		return upstream
				.zipWith(trueFalse, Pair::of)
				.filter(Pair::getRight)
				.map(Pair::getLeft);
	}

	private <T> Observable.Transformer<T, T> odd() {
		Observable<Boolean> trueFalse = just(true, false).repeat();
		return upstream -> upstream
				.zipWith(trueFalse, Pair::of)
				.filter(Pair::getRight)
				.map(Pair::getLeft);
	}
	
	@Test
	public void sample_618() throws Exception {
		//[A, B, C, D, E...]
		Observable<Character> alphabet =
				Observable
						.range(0, 'Z' - 'A' + 1)
						.map(c -> (char) ('A' + c));

		//[A, C, E, G, I...]
		alphabet
				.compose(odd())
				.forEach(System.out::println);

	}

	@Test
	public void sample_9() throws Exception {
		Observable
				.range(1, 1000)
				.filter(x -> x % 3 == 0)
				.distinct()
				.reduce((a, x) -> a + x)
				.map(Integer::toHexString)
				.subscribe(System.out::println);
	}

	@Test
	public void sample_59() throws Exception {
		Observable<String> odd = Observable
				.range(1, 9)
				.lift(toStringOfOdd());
		//Will emit: "1", "3", "5", "7" and "9" strings

		odd.subscribe(System.out::println);
	}

	<T> Observable.Operator<String, T> toStringOfOdd() {
		return new Observable.Operator<String, T>() {

			private boolean odd = true;

			@Override
			public Subscriber<? super T> call(Subscriber<? super String> child) {
				return new Subscriber<T>(child) {
					@Override
					public void onCompleted() {
						child.onCompleted();
					}

					@Override
					public void onError(Throwable e) {
						child.onError(e);
					}

					@Override
					public void onNext(T t) {
						if(odd) {
							child.onNext(t.toString());
						} else {
							request(1);
						}
						odd = !odd;
					}
				};
			}
		};
	}


	@Test
	public void sample_67() throws Exception {
		Observable
				.range(1, 9)
				.buffer(1, 2)
				.concatMapIterable(x -> x)
				.map(Object::toString);
	}

	@Test
	public void sample_112() throws Exception {
		Observable
				.range(1, 4)
				.repeat()
				.lift(toStringOfOdd())
				.take(3)
				.subscribe(
						System.out::println,
						Throwable::printStackTrace,
						() -> System.out.println("Completed")
				);
	}
}
