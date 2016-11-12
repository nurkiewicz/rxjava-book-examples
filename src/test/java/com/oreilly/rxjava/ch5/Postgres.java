package com.oreilly.rxjava.ch5;

import org.junit.Ignore;
import org.junit.Test;
import org.postgresql.PGNotification;
import org.postgresql.jdbc2.AbstractJdbc2Connection;
import org.postgresql.jdbc42.Jdbc42Connection;
import rx.Observable;
import rx.observers.Subscribers;
import rx.subscriptions.Subscriptions;

import java.sql.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Ignore
public class Postgres {


	@Test
	public void sample_37() throws Exception {
		try (
				Connection conn = DriverManager.getConnection("jdbc:h2:mem:");
				Statement stat = conn.createStatement();
				ResultSet rs = stat.executeQuery("SELECT 2 + 2 AS total")
		) {
			if (rs.next()) {
				System.out.println(rs.getInt("total"));
				assert rs.getInt("total") == 4;
			}
		}
	}

	@Test
	public void sample_55() throws Exception {
		try (Connection connection =
				     DriverManager.getConnection("jdbc:postgresql:db")) {
			try (Statement statement = connection.createStatement()) {
				statement.execute("LISTEN my_channel");
			}
			Jdbc42Connection pgConn = (Jdbc42Connection) connection;
			pollForNotifications(pgConn);
		}
	}

	void pollForNotifications(Jdbc42Connection pgConn) throws Exception {
		while (!Thread.currentThread().isInterrupted()) {
			final PGNotification[] notifications = pgConn.getNotifications();
			if (notifications != null) {
				for (final PGNotification notification : notifications) {
					System.out.println(
							notification.getName() + ": " +
									notification.getParameter());
				}
			}
			TimeUnit.MILLISECONDS.sleep(100);
		}
	}

	Observable<PGNotification> observe(String channel, long pollingPeriod) {
		return Observable.<PGNotification>create(subscriber -> {
			try {
				Connection connection = DriverManager
						.getConnection("jdbc:postgresql:db");
				subscriber.add(Subscriptions.create(() ->
						closeQuietly(connection)));
				listenOn(connection, channel);
				Jdbc42Connection pgConn = (Jdbc42Connection) connection;
				pollForNotifications(pollingPeriod, pgConn)
						.subscribe(Subscribers.wrap(subscriber));
			} catch (Exception e) {
				subscriber.onError(e);
			}
		}).share();
	}

	void listenOn(Connection connection, String channel) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("LISTEN " + channel);
		}
	}

	void closeQuietly(Connection connection) {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	Observable<PGNotification> pollForNotifications(
			long pollingPeriod,
			AbstractJdbc2Connection pgConn) {
		return Observable
				.interval(0, pollingPeriod, TimeUnit.MILLISECONDS)
				.flatMap(x -> tryGetNotification(pgConn))
				.filter(arr -> arr != null)
				.flatMapIterable(Arrays::asList);
	}

	Observable<PGNotification[]> tryGetNotification(
			AbstractJdbc2Connection pgConn) {
		try {
			return Observable.just(pgConn.getNotifications());
		} catch (SQLException e) {
			return Observable.error(e);
		}
	}


}
