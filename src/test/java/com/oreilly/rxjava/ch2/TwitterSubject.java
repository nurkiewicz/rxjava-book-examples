package com.oreilly.rxjava.ch2;

import rx.Observable;
import rx.subjects.PublishSubject;
import twitter4j.*;

class TwitterSubject {

	private final PublishSubject<Status> subject = PublishSubject.create();

	public TwitterSubject() {
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		twitterStream.addListener(new StatusListener() {
			@Override
			public void onStatus(Status status) {
				subject.onNext(status);
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
				subject.onError(ex);
			}

		});
		twitterStream.sample();
	}

	public Observable<Status> observe() {
		return subject;
	}

}
