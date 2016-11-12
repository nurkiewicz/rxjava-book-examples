package com.oreilly.rxjava.ch3;

import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;
import rx.observables.GroupedObservable;

import java.util.Optional;
import java.util.UUID;

@Ignore
public class Reservations {

	@Test
	public void sample_9() throws Exception {
		FactStore factStore = new CassandraFactStore();
		Observable<ReservationEvent> facts = factStore.observe();
		facts.subscribe(this::updateProjection);
	}

	void updateProjection(ReservationEvent event) {

	}

	private void store(UUID id, Reservation modified) {
		//...
	}

	Optional<Reservation> loadBy(UUID uuid) {
		//...
		return Optional.of(new Reservation());
	}

	@Test
	public void sample_34() throws Exception {
		FactStore factStore = new CassandraFactStore();

		Observable<ReservationEvent> facts = factStore.observe();

		facts
				.flatMap(this::updateProjectionAsync)
				.subscribe();

		//...
	}

	Observable<ReservationEvent> updateProjectionAsync(ReservationEvent event) {
		//possibly asynchronous
		return Observable.just(new ReservationEvent());
	}

	@Test
	public void sample_52() throws Exception {
		FactStore factStore = new CassandraFactStore();

		Observable<ReservationEvent> facts = factStore.observe();

		Observable<GroupedObservable<UUID, ReservationEvent>> grouped =
				facts.groupBy(ReservationEvent::getReservationUuid);

		grouped.subscribe(byUuid -> {
			byUuid.subscribe(this::updateProjection);
		});
	}

}
