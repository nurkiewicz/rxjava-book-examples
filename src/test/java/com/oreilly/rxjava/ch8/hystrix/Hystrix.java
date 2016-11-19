package com.oreilly.rxjava.ch8.hystrix;

import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.metric.HystrixCommandCompletionStream;
import com.oreilly.rxjava.ch8.Cities;
import com.oreilly.rxjava.ch8.MeetupApi;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.netflix.hystrix.HystrixEventType.FAILURE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@Ignore
public class Hystrix {

	private static final Logger log = LoggerFactory.getLogger(Hystrix.class);

	@Test
	public void sample_17() throws Exception {
		String string = new BlockingCmd().execute();
		Future<String> future = new BlockingCmd().queue();
		Observable<String> eager = new BlockingCmd().observe();
		Observable<String> lazy = new BlockingCmd().toObservable();
	}

	@Test
	public void sample_28() throws Exception {
		Observable<String> retried = new BlockingCmd()
				.toObservable()
				.doOnError(ex -> log.warn("Error ", ex))
				.retryWhen(ex -> ex.delay(500, MILLISECONDS))
				.timeout(3, SECONDS);
	}

	@Test
	public void sample_44() throws Exception {
		MeetupApi api = mock(MeetupApi.class);
		given(api.listCities(anyDouble(), anyDouble())).willReturn(
				Observable
						.<Cities>error(new RuntimeException("Broken"))
						.doOnSubscribe(() -> log.debug("Invoking"))
						.delay(2, SECONDS)
		);

		Observable
				.interval(50, MILLISECONDS)
				.doOnNext(x -> log.debug("Requesting"))
				.flatMap(x ->
								new CitiesCmd(api, 52.229841, 21.011736)
										.toObservable()
										.onErrorResumeNext(ex -> Observable.empty()),
						5);
	}

	Observable<Book> allBooks() {
		return Observable.just(new Book());
	}
	Observable<Rating> fetchRating(Book book) {
		return Observable.just(new Rating(book));
	}

	@Test
	public void sample_69() throws Exception {
		Observable<Rating> ratings = allBooks()
				.flatMap(this::fetchRating);
	}

	@Test
	public void sample_83() throws Exception {
		final Book book = new Book();
		Observable<Rating> ratingObservable =
				new FetchRatingsCollapser(book).toObservable();
	}

	@Test
	public void sample_82() throws Exception {
		HystrixCommandCompletionStream
				.getInstance(HystrixCommandKey.Factory.asKey("FetchRating"))
				.observe()
				.filter(e -> e.getEventCounts().getCount(FAILURE) > 0)
				.window(1, TimeUnit.SECONDS)
				.flatMap(Observable::count)
				.subscribe(x -> log.info("{} failures/s", x));
	}

	@Test
	public void sample_98() throws Exception {
		ServletContextHandler context = new ServletContextHandler(NO_SESSIONS);
		HystrixMetricsStreamServlet servlet = new HystrixMetricsStreamServlet();
		context.addServlet(new ServletHolder(servlet), "/hystrix.stream");
		Server server = new Server(8080);
		server.setHandler(context);
		server.start();
		server.join();
	}

}
