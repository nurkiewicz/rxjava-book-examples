package com.oreilly.rxjava.ch5;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.w3c.dom.Document;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.time.Instant;

@Ignore
public class Singles {

	@Test
	public void sample_6() throws Exception {
		Single<String> single = Single.just("Hello, world!");
		single.subscribe(System.out::println);

		Single<Instant> error =
				Single.error(new RuntimeException("Opps!"));
		error
				.observeOn(Schedulers.io())
				.subscribe(
						System.out::println,
						Throwable::printStackTrace
				);
	}

	AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

	Single<Response> fetch(String address) {
		return Single.create(subscriber ->
				asyncHttpClient
						.prepareGet(address)
						.execute(handler(subscriber)));
	}

	AsyncCompletionHandler handler(SingleSubscriber<? super Response> subscriber) {
		return new AsyncCompletionHandler() {
			public Response onCompleted(Response response) {
				subscriber.onSuccess(response);
				return response;
			}

			public void onThrowable(Throwable t) {
				subscriber.onError(t);
			}
		};
	}

	@Test
	public void sample_55() throws Exception {
		Single<String> example =
				fetch("http://www.example.com")
						.flatMap(this::body);

		String b = example.toBlocking().value();
	}

	Single<String> body(Response response) {
		return Single.create(subscriber -> {
			try {
				subscriber.onSuccess(response.getResponseBody());
			} catch (IOException e) {
				subscriber.onError(e);
			}
		});
	}

	//Same functionality as body():
	Single<String> body2(Response response) {
		return Single.fromCallable(() ->
				response.getResponseBody());
	}

	private final JdbcTemplate jdbcTemplate = new JdbcTemplate();

	Single<String> content(int id) {
		return Single.fromCallable(() -> jdbcTemplate
				.queryForObject(
						"SELECT content FROM articles WHERE id = ?",
						String.class, id))
				.subscribeOn(Schedulers.io());
	}

	Single<Integer> likes(int id) {
		//asynchronous HTTP request to social media website
		return Single.just(7);
	}

	Single<Void> updateReadCount() {
		//only side effect, no return value in Single
		return Single.just(null);
	}

	@Test
	public void sample_98() throws Exception {
		Single<Document> doc = Single.zip(
				content(123),
				likes(123),
				updateReadCount(),
				(con, lks, vod) -> buildHtml(con, lks)
		);
	}

	Document buildHtml(String content, int likes) {
		//...
		return null;
	}

	@Test
	public void sample_113() throws Exception {
		Single<String> single = Single.create(subscriber -> {
			System.out.println("Subscribing");
			subscriber.onSuccess("42");
		});

		Single<String> cachedSingle = single
				.toObservable()
				.cache()
				.toSingle();

		cachedSingle.subscribe(System.out::println);
		cachedSingle.subscribe(System.out::println);
	}

	@Test
	public void sample_129() throws Exception {
		Single<Integer> emptySingle =
				Observable.<Integer>empty().toSingle();
		Single<Integer> doubleSingle =
				Observable.just(1, 2).toSingle();
	}

	@Test
	public void sample_138() throws Exception {
		Single<Integer> ignored = Single
				.just(1)
				.toObservable()
				.ignoreElements()   //PROBLEM
				.toSingle();
	}

}
