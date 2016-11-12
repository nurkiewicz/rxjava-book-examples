package com.oreilly.rxjava.ch2;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;
import rx.observables.ConnectableObservable;
import rx.subscriptions.Subscriptions;
import twitter4j.*;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Ignore
public class TwitterSample {

	private static final Logger log = LoggerFactory.getLogger(TwitterSample.class);

	@Test
	public void sample_18() throws Exception {
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		twitterStream.addListener(new twitter4j.StatusListener() {
			@Override
			public void onStatus(Status status) {
				log.info("Status: {}", status);
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				//...
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				//...
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				//...
			}

			@Override
			public void onStallWarning(StallWarning warning) {
				//...
			}

			@Override
			public void onException(Exception ex) {
				log.error("Error callback", ex);
			}

			//other callbacks
		});
		twitterStream.sample();
		TimeUnit.SECONDS.sleep(10);
		twitterStream.shutdown();
	}

	void consume(
			Consumer<Status> onStatus,
			Consumer<Exception> onException) {
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		twitterStream.addListener(new StatusListener() {
			@Override
			public void onStatus(Status status) {
				onStatus.accept(status);
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				//...
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				//...
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				//...
			}

			@Override
			public void onStallWarning(StallWarning warning) {
				//...
			}

			@Override
			public void onException(Exception ex) {
				onException.accept(ex);
			}
		});
		twitterStream.sample();
	}

	@Test
	public void sample_99() throws Exception {
		consume(
				status -> log.info("Status: {}", status),
				ex -> log.error("Error callback", ex)
		);
	}

	Observable<Status> observe() {
		return Observable.create(subscriber -> {
			TwitterStream twitterStream =
					new TwitterStreamFactory().getInstance();
			twitterStream.addListener(new StatusListener() {
				@Override
				public void onStatus(Status status) {
					if (subscriber.isUnsubscribed()) {
						twitterStream.shutdown();
					} else {
						subscriber.onNext(status);
					}
				}

				@Override
				public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
					//...
				}

				@Override
				public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
					//...
				}

				@Override
				public void onScrubGeo(long userId, long upToStatusId) {
					//...
				}

				@Override
				public void onStallWarning(StallWarning warning) {
					//...
				}

				@Override
				public void onException(Exception ex) {
					if (subscriber.isUnsubscribed()) {
						twitterStream.shutdown();
					} else {
						subscriber.onError(ex);
					}
				}
			});
			subscriber.add(Subscriptions.create(twitterStream::shutdown));
		});
	}

	@Test
	public void sample_150() throws Exception {
		observe().subscribe(
				status -> log.info("Status: {}", status),
				ex -> log.error("Error callback", ex)
		);
	}

	@Test
	public void sample_162() throws Exception {
		Observable<Status> observable = status();

		Subscription sub1 = observable.subscribe();
		System.out.println("Subscribed 1");
		Subscription sub2 = observable.subscribe();
		System.out.println("Subscribed 2");
		sub1.unsubscribe();
		System.out.println("Unsubscribed 1");
		sub2.unsubscribe();
		System.out.println("Unsubscribed 2");
	}

	private Observable<Status> status() {
		return Observable.create(subscriber -> {
			System.out.println("Establishing connection");
			TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
			//...
			subscriber.add(Subscriptions.create(() -> {
				System.out.println("Disconnecting");
				twitterStream.shutdown();
			}));
			twitterStream.sample();
		});
	}

	@Test
	public void sample_186() throws Exception {
		Observable<Status> observable = status();
		Observable<Status> lazy = observable.publish().refCount();
		//...
		System.out.println("Before subscribers");
		Subscription sub1 = lazy.subscribe();
		System.out.println("Subscribed 1");
		Subscription sub2 = lazy.subscribe();
		System.out.println("Subscribed 2");
		sub1.unsubscribe();
		System.out.println("Unsubscribed 1");
		sub2.unsubscribe();
		System.out.println("Unsubscribed 2");
	}

	@Test
	public void sample_206() throws Exception {
		final Observable<Status> tweets = status();
		ConnectableObservable<Status> published = tweets.publish();
		published.connect();
	}

}
