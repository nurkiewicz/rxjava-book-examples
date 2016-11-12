package com.oreilly.rxjava.ch4;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static rx.Observable.fromCallable;

@Ignore
public class Flights {

	private static final Logger log = LoggerFactory.getLogger(Flights.class);

	Flight lookupFlight(String flightNo) {
		//...
		return new Flight();
	}

	Passenger findPassenger(long id) {
		//...
		return new Passenger();
	}

	Ticket bookTicket(Flight flight, Passenger passenger) {
		//...
		return new Ticket();
	}

	SmtpResponse sendEmail(Ticket ticket) {
		//...
		return new SmtpResponse();
	}

	@Test
	public void sample_29() throws Exception {
		Flight flight = lookupFlight("LOT 783");
		Passenger passenger = findPassenger(42);
		Ticket ticket = bookTicket(flight, passenger);
		sendEmail(ticket);
	}

	Observable<Flight> rxLookupFlight(String flightNo) {
		return Observable.defer(() ->
				Observable.just(lookupFlight(flightNo)));
	}

	Observable<Passenger> rxFindPassenger(long id) {
		return Observable.defer(() ->
				Observable.just(findPassenger(id)));
	}

	@Test
	public void sample_49() throws Exception {
		Observable<Flight> flight = rxLookupFlight("LOT 783");
		Observable<Passenger> passenger = rxFindPassenger(42);
		Observable<Ticket> ticket =
				flight.zipWith(passenger, (f, p) -> bookTicket(f, p));
		ticket.subscribe(this::sendEmail);
	}

	@Test
	public void sample_67() throws Exception {
		rxLookupFlight("LOT 783")
				.subscribeOn(Schedulers.io())
				.timeout(100, TimeUnit.MILLISECONDS);
	}

	@Test
	public void sample_76() throws Exception {
		Observable<Flight> flight =
				rxLookupFlight("LOT 783").subscribeOn(Schedulers.io());
		Observable<Passenger> passenger =
				rxFindPassenger(42).subscribeOn(Schedulers.io());

		Observable<Ticket> ticket = flight
				.zipWith(passenger, (Flight f, Passenger p) -> Pair.of(f, p))
				.flatMap(pair -> rxBookTicket(pair.getLeft(), pair.getRight()));
	}

	private Observable<Ticket> rxBookTicket(Flight left, Passenger right) {
		return Observable.just(new Ticket());
	}

	@Test
	public void sample_85() throws Exception {
		Observable<Flight> flight =
				rxLookupFlight("LOT 783").subscribeOn(Schedulers.io());
		Observable<Passenger> passenger =
				rxFindPassenger(42).subscribeOn(Schedulers.io());

		Observable<Ticket> ticket = flight
				.zipWith(passenger, this::rxBookTicket)
				.flatMap(obs -> obs);
	}

	@Test
	public void sample_97() throws Exception {
		List<Ticket> tickets = Arrays.asList(new Ticket(), new Ticket(), new Ticket());
		List<Ticket> failures = new ArrayList<>();
		for (Ticket ticket : tickets) {
			try {
				sendEmail(ticket);
			} catch (Exception e) {
				log.warn("Failed to send {}", ticket, e);
				failures.add(ticket);
			}
		}
	}

	@Test
	public void sample_120() throws Exception {
		List<Ticket> tickets = Arrays.asList(new Ticket(), new Ticket(), new Ticket());

		List<Pair<Ticket, Future<SmtpResponse>>> tasks = tickets
				.stream()
				.map(ticket -> Pair.of(ticket, sendEmailAsync(ticket)))
				.collect(toList());

		List<Ticket> failures = tasks.stream()
				.flatMap(pair -> {
					try {
						Future<SmtpResponse> future = pair.getRight();
						future.get(1, TimeUnit.SECONDS);
						return Stream.empty();
					} catch (Exception e) {
						Ticket ticket = pair.getLeft();
						log.warn("Failed to send {}", ticket, e);
						return Stream.of(ticket);
					}
				})
				.collect(toList());
	}

	private Future<SmtpResponse> sendEmailAsync(Ticket ticket) {
		return CompletableFuture.supplyAsync(() -> sendEmail(ticket));
	}

	@Test
	public void sample_152() throws Exception {
		List<Ticket> tickets = Arrays.asList(new Ticket(), new Ticket(), new Ticket());

		//WARNING: code is sequential despite utilizing thread pool
		tickets
				.stream()
				.map(ticket -> Pair.of(ticket, sendEmailAsync(ticket)))
				.map(pair -> {
					try {
						return pair.getRight().get();
					} catch (InterruptedException | ExecutionException e) {
						throw new RuntimeException(e);
					}
				})
				.collect(toList());
	}

	Observable<SmtpResponse> rxSendEmail(Ticket ticket) {
		//unusual synchronous Observable
		return fromCallable(() -> sendEmail(ticket));
	}

	@Test
	public void sample_177() throws Exception {
		List<Ticket> tickets = Arrays.asList(new Ticket(), new Ticket(), new Ticket());
		List<Ticket> failures = Observable.from(tickets)
				.flatMap(ticket ->
						rxSendEmail(ticket)
								.flatMap(response -> Observable.<Ticket>empty())
								.doOnError(e -> log.warn("Failed to send {}", ticket, e))
								.onErrorReturn(err -> ticket))
				.toList()
				.toBlocking()
				.single();
	}

	@Test
	public void sample_191() throws Exception {
		List<Ticket> tickets = Arrays.asList(new Ticket(), new Ticket(), new Ticket());

		Observable
				.from(tickets)
				.flatMap(ticket ->
						rxSendEmail(ticket)
								.ignoreElements()
								.doOnError(e -> log.warn("Failed to send {}", ticket, e))
								.subscribeOn(Schedulers.io()));
	}



}
