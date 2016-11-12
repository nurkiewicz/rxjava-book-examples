package com.oreilly.rxjava.ch4;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

import javax.jms.*;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;

@Ignore
public class Messaging {

	private static final Logger log = LoggerFactory.getLogger(Messaging.class);

	void connect() {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		Observable<String> txtMessages = observe(connectionFactory, new ActiveMQTopic("orders"))
				.cast(TextMessage.class)
				.flatMap(m -> {
					try {
						return Observable.just(m.getText());
					} catch (JMSException e) {
						return Observable.error(e);
					}
				});
	}

	public Observable<Message> observe(ConnectionFactory connectionFactory, Topic topic) {
		return Observable.create(subscriber -> {
			try {
				subscribeThrowing(subscriber, connectionFactory, topic);
			} catch (JMSException e) {
				subscriber.onError(e);
			}
		});
	}

	private void subscribeThrowing(Subscriber<? super Message> subscriber, ConnectionFactory connectionFactory, Topic orders) throws JMSException {
		Connection connection = connectionFactory.createConnection();
		Session session = connection.createSession(true, AUTO_ACKNOWLEDGE);
		MessageConsumer consumer = session.createConsumer(orders);
		consumer.setMessageListener(subscriber::onNext);
		subscriber.add(onUnsubscribe(connection));
		connection.start();
	}

	private Subscription onUnsubscribe(Connection connection) {
		return Subscriptions.create(() -> {
			try {
				connection.close();
			} catch (Exception e) {
				log.error("Can't close", e);
			}
		});
	}

}
