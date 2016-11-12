package com.oreilly.rxjava.ch7;

import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import rx.Notification;
import rx.Observable;
import rx.Scheduler;
import rx.observables.BlockingObservable;
import rx.observables.SyncOnSubscribe;
import rx.observers.TestSubscriber;
import rx.plugins.RxJavaPlugins;
import rx.plugins.RxJavaSchedulersHook;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.failBecauseExceptionWasNotThrown;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static rx.Notification.Kind.OnError;
import static rx.Notification.Kind.OnNext;
import static rx.Observable.fromCallable;

@Ignore
public class Testing {

	@Test
	public void sample_9() throws Exception {
		TestScheduler sched = Schedulers.test();
		Observable<String> fast = Observable
				.interval(10, MILLISECONDS, sched)
				.map(x -> "F" + x)
				.take(3);
		Observable<String> slow = Observable
				.interval(50, MILLISECONDS, sched)
				.map(x -> "S" + x);

		Observable<String> stream = Observable.concat(fast, slow);
		stream.subscribe(System.out::println);
		System.out.println("Subscribed");
	}

	@Test
	public void sample_31() throws Exception {
		TestScheduler sched = Schedulers.test();

		TimeUnit.SECONDS.sleep(1);
		System.out.println("After one second");
		sched.advanceTimeBy(25, MILLISECONDS);

		TimeUnit.SECONDS.sleep(1);
		System.out.println("After one more second");
		sched.advanceTimeBy(75, MILLISECONDS);

		TimeUnit.SECONDS.sleep(1);
		System.out.println("...and one more");
		sched.advanceTimeTo(200, MILLISECONDS);
	}

	@Test
	public void shouldApplyConcatMapInOrder() throws Exception {
		List<String> list = Observable
				.range(1, 3)
				.concatMap(x -> Observable.just(x, -x))
				.map(Object::toString)
				.toList()
				.toBlocking()
				.single();

		assertThat(list).containsExactly("1", "-1", "2", "-2", "3", "-3");
	}

	@Test
	public void sample_65() throws Exception {
		File file = new File("404.txt");
		BlockingObservable<String> fileContents =
				fromCallable(() -> Files.toString(file, UTF_8))
				.toBlocking();

		try {
			fileContents.single();
			failBecauseExceptionWasNotThrown(FileNotFoundException.class);
		} catch (RuntimeException expected) {
			assertThat(expected)
					.hasCauseInstanceOf(FileNotFoundException.class);
		}
	}

	@Test
	public void sample_87() throws Exception {
		Observable<Notification<Integer>> notifications = Observable
				.just(3, 0, 2, 0, 1, 0)
				.concatMapDelayError(x -> fromCallable(() -> 100 / x))
				.materialize();

		List<Notification.Kind> kinds = notifications
				.map(Notification::getKind)
				.toList()
				.toBlocking()
				.single();

		assertThat(kinds).containsExactly(OnNext, OnNext, OnNext, OnError);
	}

	@Test
	public void sample_107() throws Exception {
		Observable<Integer> obs = Observable
				.just(3, 0, 2, 0, 1, 0)
				.concatMapDelayError(x -> Observable.fromCallable(() -> 100 / x));

		TestSubscriber<Integer> ts = new TestSubscriber<>();
		obs.subscribe(ts);

		ts.assertValues(33, 50, 100);
		ts.assertError(ArithmeticException.class);  //Fails (!)
	}

	private MyServiceWithTimeout mockReturning(
			Observable<LocalDate> result,
			TestScheduler testScheduler) {
		MyService mock = mock(MyService.class);
		given(mock.externalCall()).willReturn(result);
		return new MyServiceWithTimeout(mock, testScheduler);
	}

	@Test
	public void timeoutWhenServiceNeverCompletes() throws Exception {
		//given
		TestScheduler testScheduler = Schedulers.test();
		MyService mock = mockReturning(
				Observable.never(), testScheduler);
		TestSubscriber<LocalDate> ts = new TestSubscriber<>();

		//when
		mock.externalCall().subscribe(ts);

		//then
		testScheduler.advanceTimeBy(950, MILLISECONDS);
		ts.assertNoTerminalEvent();
		testScheduler.advanceTimeBy(100, MILLISECONDS);
		ts.assertCompleted();
		ts.assertNoValues();
	}

	@Test
	public void valueIsReturnedJustBeforeTimeout() throws Exception {
		//given
		TestScheduler testScheduler = Schedulers.test();
		Observable<LocalDate> slow = Observable
				.timer(950, MILLISECONDS, testScheduler)
				.map(x -> LocalDate.now());
		MyService myService = mockReturning(slow, testScheduler);
		TestSubscriber<LocalDate> ts = new TestSubscriber<>();

		//when
		myService.externalCall().subscribe(ts);

		//then
		testScheduler.advanceTimeBy(930, MILLISECONDS);
		ts.assertNotCompleted();
		ts.assertNoValues();
		testScheduler.advanceTimeBy(50, MILLISECONDS);
		ts.assertCompleted();
		ts.assertValueCount(1);
	}

	private final TestScheduler testScheduler = new TestScheduler();

	@Before
	public void alwaysUseTestScheduler() {
		RxJavaPlugins
				.getInstance()
				.registerSchedulersHook(new RxJavaSchedulersHook() {
					@Override
					public Scheduler getComputationScheduler() {
						return testScheduler;
					}

					@Override
					public Scheduler getIOScheduler() {
						return testScheduler;
					}

					@Override
					public Scheduler getNewThreadScheduler() {
						return testScheduler;
					}
				});
	}

	Observable<Long> naturals1() {
		return Observable.create(subscriber -> {
			long i = 0;
			while (!subscriber.isUnsubscribed()) {
				subscriber.onNext(i++);
			}
		});
	}

	Observable<Long> naturals2() {
		return Observable.create(
				SyncOnSubscribe.createStateful(
						() -> 0L,
						(cur, observer) -> {
							observer.onNext(cur);
							return cur + 1;
						}
				));
	}

	@Test
	public void sample_222() throws Exception {
		TestSubscriber<Long> ts = new TestSubscriber<>(0);

		naturals1()
				.take(10)
				.subscribe(ts);

		ts.assertNoValues();
		ts.requestMore(100);
		ts.assertValueCount(10);
		ts.assertCompleted();
	}

}
