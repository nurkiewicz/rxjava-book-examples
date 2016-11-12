package com.oreilly.rxjava.ch3;

import java.util.UUID;

class ReservationEvent {
	private final UUID uuid = UUID.randomUUID();
	public UUID getReservationUuid() {
		return uuid;
	}
}
