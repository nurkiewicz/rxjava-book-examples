package com.oreilly.rxjava.ch4;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.subjects.PublishSubject;

@Component
class JmsConsumer {

    private final PublishSubject<Message> subject = PublishSubject.create();

    @JmsListener(destination = "orders", concurrency="1")
    public void newOrder(Message msg) {
        subject.onNext(msg);
    }

    Observable<Message> observe() {
        return subject;
    }

}
