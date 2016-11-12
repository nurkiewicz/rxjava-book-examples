package com.oreilly.rxjava.ch5;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.UnaryOperator.identity;

@Ignore
public class CompletableFutures {

	User findById(long id) {
		return new User();
	}

	GeoLocation locate() {
		return new GeoLocation();
	}

	Ticket book(Flight flight) {
		return new Ticket();
	}

	@Test
	public void sample_21() throws Exception {
		long id = 42;

		ExecutorService pool = Executors.newFixedThreadPool(10);
		List<TravelAgency> agencies = Collections.singletonList(new SomeTravelAgency());

		User user = findById(id);
		GeoLocation location = locate();
		ExecutorCompletionService<Flight> ecs = new ExecutorCompletionService<>(pool);
		agencies.forEach(agency ->
				ecs.submit(() ->
						agency.search(user, location)));
		Future<Flight> firstFlight = ecs.poll(5, SECONDS);
		Flight flight = firstFlight.get();
		book(flight);
	}

	CompletableFuture<User> findByIdAsync(long id) {
		return CompletableFuture.supplyAsync(() -> findById(id));
	}

	CompletableFuture<GeoLocation> locateAsync() {
		return CompletableFuture.supplyAsync(this::locate);
	}

	CompletableFuture<Ticket> bookAsync(Flight flight) {
		return CompletableFuture.supplyAsync(() -> book(flight));
	}

	Observable<User> rxFindById(long id) {
		return Util.observe(findByIdAsync(id));
	}

	Observable<GeoLocation> rxLocate() {
		return Util.observe(locateAsync());
	}

	Observable<Ticket> rxBook(Flight flight) {
		return Util.observe(bookAsync(flight));
	}

	@Test
	public void sample_63() throws Exception {
		long id = 42;
		List<TravelAgency> agencies = Collections.singletonList(new SomeTravelAgency());
		CompletableFuture<User> user = findByIdAsync(id);
		CompletableFuture<GeoLocation> location = locateAsync();

		CompletableFuture<Ticket> ticketFuture = user
				.thenCombine(location, (User us, GeoLocation loc) -> agencies
						.stream()
						.map(agency -> agency.searchAsync(us, loc))
						.reduce((f1, f2) ->
								f1.applyToEither(f2, identity())
						)
						.get()
				)
				.thenCompose(identity())
				.thenCompose(this::bookAsync);
	}

	@Test
	public void sample_80() throws Exception {
		CompletableFuture<Long> timeFuture = CompletableFuture.completedFuture(System.currentTimeMillis());
		CompletableFuture<ZoneId> zoneFuture = CompletableFuture.completedFuture(ZoneId.of("GMT"));

		CompletableFuture<Instant> instantFuture = timeFuture
				.thenApply(time -> Instant.ofEpochMilli(time));

		CompletableFuture<ZonedDateTime> zdtFuture = instantFuture
				.thenCombine(zoneFuture, (instant, zoneId) ->
						ZonedDateTime.ofInstant(instant, zoneId));
	}

	@Test
	public void sample_96() throws Exception {
		List<TravelAgency> agencies = Collections.singletonList(new SomeTravelAgency());

		User us = new User();
		GeoLocation loc = new GeoLocation();
		agencies
				.stream()
				.map(agency -> agency.searchAsync(us, loc))
				.reduce((f1, f2) ->
						f1.applyToEither(f2, identity())
				)
				.get();

	}

	@Test
	public void sample_111() throws Exception {
		CompletableFuture<User> primaryFuture = CompletableFuture.completedFuture(new User());
		CompletableFuture<User> secondaryFuture = CompletableFuture.completedFuture(new User());

		CompletableFuture<LocalDate> ageFuture =
				primaryFuture
						.applyToEither(secondaryFuture,
								user -> user.getBirth());
	}

	@Test
	public void sample_123() throws Exception {
		CompletableFuture<Flight> flightFuture = CompletableFuture.completedFuture(new Flight());

		CompletableFuture<Ticket> ticketFuture = flightFuture
				.thenCompose(flight -> bookAsync(flight));

	}

	@Test
	public void sample_145() throws Exception {
		long id = 42;

		Observable<TravelAgency> agencies = agencies();
		Observable<User> user = rxFindById(id);
		Observable<GeoLocation> location = rxLocate();

		Observable<Ticket> ticket = user
				.zipWith(location, (us, loc) ->
						agencies
								.flatMap(agency -> agency.rxSearch(us, loc))
								.first()
				)
				.flatMap(x -> x)
				.flatMap(this::rxBook);

	}

	private Observable<TravelAgency> agencies() {
		return Observable.just(new SomeTravelAgency());
	}

	@Test
	public void sample_168() throws Exception {
		Observable<User> user = Observable.just(new User());
		Observable<GeoLocation> location = Observable.just(new GeoLocation());
		Observable<TravelAgency> agencies = agencies();

		Observable<Ticket> ticket = user
				.zipWith(location, (usr, loc) -> Pair.of(usr, loc))
				.flatMap(pair -> agencies
						.flatMap(agency -> {
							User usr = pair.getLeft();
							GeoLocation loc = pair.getRight();
							return agency.rxSearch(usr, loc);
						}))
				.first()
				.flatMap(this::rxBook);

	}

}


