package com.oreilly.rxjava.ch4;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.oreilly.rxjava.util.Sleeper;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;
import rx.observables.BlockingObservable;
import rx.schedulers.Schedulers;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;


@Ignore
public class Chapter4 {

	private static final Logger log = LoggerFactory.getLogger(Chapter4.class);

	private final PersonDao personDao = new PersonDao();
	private int orderBookLength;

	@Test
	public void sample_9() throws Exception {
		List<Person> people = personDao.listPeople();
		String json = marshal(people);
	}

	@Test
	public void sample_20() throws Exception {
		Observable<Person> peopleStream = personDao.listPeople2();
		Observable<List<Person>> peopleList = peopleStream.toList();
		BlockingObservable<List<Person>> peopleBlocking = peopleList.toBlocking();
		List<Person> people = peopleBlocking.single();
	}

	private String marshal(List<Person> people) {
		return people.toString();
	}

	@Test
	public void sample_34() throws Exception {
		List<Person> people = personDao
				.listPeople2()
				.toList()
				.toBlocking()
				.single();
	}

	void bestBookFor(Person person) {
		Book book;
		try {
			book = recommend(person);
		} catch (Exception e) {
			book = bestSeller();
		}
		display(book.getTitle());
	}

	private Book bestSeller() {
		return new Book();
	}

	private Book recommend(Person person) {
		return new Book();
	}

	void display(String title) {
		//...
	}

	void bestBookFor2(Person person) {
		Observable<Book> recommended = recommend2(person);
		Observable<Book> bestSeller = bestSeller2();
		Observable<Book> book = recommended.onErrorResumeNext(bestSeller);
		Observable<String> title = book.map(Book::getTitle);
		title.subscribe(this::display);
	}

	void bestBookFor3(Person person) {
		recommend2(person)
				.onErrorResumeNext(bestSeller2())
				.map(Book::getTitle)
				.subscribe(this::display);
	}

	private Observable<Book> bestSeller2() {
		return Observable.fromCallable(Book::new);
	}

	private Observable<Book> recommend2(Person person) {
		return Observable.fromCallable(Book::new);
	}

	@Test
	public void sample_89() throws Exception {
		Observable
				.interval(10, TimeUnit.MILLISECONDS)
				.map(x -> getOrderBookLength())
				.distinctUntilChanged();
	}

	private int getOrderBookLength() {
		return RandomUtils.nextInt(5, 10);
	}

	Observable<Item> observeNewItems() {
		return Observable
				.interval(1, TimeUnit.SECONDS)
				.flatMapIterable(x -> query())
				.distinct();
	}

	List<Item> query() {
		//take snapshot of file system directory
		//or database table
		return Collections.emptyList();
	}

	@Test
	public void sample_118() throws Exception {
		ThreadFactory threadFactory = new ThreadFactoryBuilder()
				.setNameFormat("MyPool-%d")
				.build();
		Executor executor = new ThreadPoolExecutor(
				10,  //corePoolSize
				10,  //maximumPoolSize
				0L, TimeUnit.MILLISECONDS, //keepAliveTime, unit
				new LinkedBlockingQueue<>(1000),  //workQueue
				threadFactory
		);
		Scheduler scheduler = Schedulers.from(executor);
	}

	@Test
	public void sample_136() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(10);
	}

	private final long start = System.currentTimeMillis();

	void log(Object label) {
		System.out.println(
				System.currentTimeMillis() - start + "\t| " +
						Thread.currentThread().getName()   + "\t| " +
						label);
	}

	@Test
	public void sample_141() throws Exception {
		Scheduler scheduler = Schedulers.immediate();
		Scheduler.Worker worker = scheduler.createWorker();

		log("Main start");
		worker.schedule(() -> {
			log(" Outer start");
			sleepOneSecond();
			worker.schedule(() -> {
				log("  Inner start");
				sleepOneSecond();
				log("  Inner end");
			});
			log(" Outer end");
		});
		log("Main end");
		worker.unsubscribe();
	}

	@Test
	public void sample_175() throws Exception {
		Scheduler scheduler = Schedulers.immediate();
		Scheduler.Worker worker = scheduler.createWorker();

		log("Main start");
		worker.schedule(() -> {
			log(" Outer start");
			sleepOneSecond();
			worker.schedule(() -> {
				log("  Middle start");
				sleepOneSecond();
				worker.schedule(() -> {
					log("   Inner start");
					sleepOneSecond();
					log("   Inner end");
				});
				log("  Middle end");
			});
			log(" Outer end");
		});
		log("Main end");
	}

	private void sleepOneSecond() {
		Sleeper.sleep(Duration.ofSeconds(1));
	}

}
