package com.oreilly.rxjava.ch8;

import com.couchbase.client.java.CouchbaseAsyncCluster;
import com.couchbase.client.java.document.AbstractDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.rx.client.MongoClients;
import com.mongodb.rx.client.MongoCollection;
import com.mongodb.rx.client.MongoDatabase;
import org.apache.camel.CamelContext;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.rx.ReactiveCamel;
import org.bson.Document;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.oreilly.rxjava.ch8.GeoNames.log;
import static java.time.Month.APRIL;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.stream.Collectors.toList;


@Ignore
public class Chapter8 {

	@Test
	public void sample_9() throws Exception {
		final ApiFactory api = new ApiFactory();
		MeetupApi meetup = api.meetup();
		GeoNames geoNames = api.geoNames();

		double warsawLat = 52.229841;
		double warsawLon = 21.011736;
		Observable<Cities> cities = meetup.listCities(warsawLat, warsawLon);
		Observable<City> cityObs = cities.concatMapIterable(Cities::getResults);

		Observable<String> map = cityObs
				.filter(city -> city.distanceTo(warsawLat, warsawLon) < 50)
				.map(City::getCity);

		Observable<Long> totalPopulation = meetup
				.listCities(warsawLat, warsawLon)
				.concatMapIterable(Cities::getResults)
				.filter(city -> city.distanceTo(warsawLat, warsawLon) < 50)
				.map(City::getCity)
				.flatMap(geoNames::populationOf)
				.reduce(0L, (x, y) -> x + y);
	}

	@Test
	public void sample_35() throws Exception {
		CouchbaseAsyncCluster cluster = CouchbaseAsyncCluster.create();
		cluster
				.openBucket("travel-sample")
				.flatMap(cl -> cl.get("route_14197")
						.map(AbstractDocument::content)
						.map(jsonObject -> jsonObject.getArray("schedule")))
				.concatMapIterable(JsonArray::toList)
				.cast(Map.class)
				.filter(m -> ((Number) m.get("day")).intValue() == 0)
				.map(m -> m.get("flight").toString())
				.subscribe(flight -> System.out.println(flight));
	}

	@Test
	public void sample_55() throws Exception {
		MongoCollection<Document> monthsColl = com.mongodb.rx.client.MongoClients
				.create()
				.getDatabase("rx")
				.getCollection("months");

		Observable
				.from(Month.values())
				.map(month -> new Document()
						.append("name", month.name())
						.append("days_not_leap", month.length(false))
						.append("days_leap", month.length(true))
				)
				.toList()
				.flatMap(monthsColl::insertMany)
				.flatMap(s -> monthsColl.find().toObservable())
				.toBlocking()
				.subscribe(System.out::println);

	}

	@Test
	public void sample_105() throws Exception {
		final MongoDatabase db = MongoClients
				.create()
				.getDatabase("rx");

		Observable<Integer> days = db.getCollection("months")
				.find(Filters.eq("name", APRIL.name()))
				.projection(Projections.include("days_not_leap"))
				.first()
				.map(doc -> doc.getInteger("days_not_leap"));
		Observable<Instant> carManufactured = db.getCollection("cars")
				.find(Filters.eq("owner.name", "Smith"))
				.first()
				.map(doc -> doc.getDate("manufactured"))
				.map(Date::toInstant);

		Observable<BigDecimal> pricePerDay = dailyPrice(LocalDateTime.now());
		Observable<Insurance> insurance = Observable
				.zip(days, carManufactured, pricePerDay,
						(d, man, price) -> {
							//Create insurance
							return new Insurance();
						});
	}

	private Observable<BigDecimal> dailyPrice(LocalDateTime date) {
		return Observable.just(BigDecimal.TEN);
	}

	@Test
	public void sample_121() throws Exception {
		CamelContext camel = new DefaultCamelContext();
		ReactiveCamel reactiveCamel = new ReactiveCamel(camel);

		reactiveCamel
				.toObservable("file:/home/user/tmp")
				.subscribe(e ->
						log.info("New file: {}", e));
	}

	@Test
	public void sample_136() throws Exception {
		CamelContext camel = new DefaultCamelContext();
		ReactiveCamel reactiveCamel = new ReactiveCamel(camel);
		reactiveCamel
				.toObservable("kafka:localhost:9092?topic=demo&groupId=rx")
				.map(Message::getBody)
				.subscribe(e ->
						log.info("Message: {}", e));
	}

	@Test
	public void sample_148() throws Exception {
		List<Person> people = Collections.emptyList(); //...

		List<String> sorted = people
				.parallelStream()
				.filter(p -> p.getAge() >= 18)
				.map(Person::getFirstName)
				.sorted(Comparator.comparing(String::toLowerCase))
				.collect(toList());

		//DON'T DO THIS
		people
				.parallelStream()
				.forEach(this::publishOverJms);
	}

	private void publishOverJms(Person person) {
		//...
	}

	@Test
	public void sample_170() throws Exception {
		Observable
				.range(0, Integer.MAX_VALUE)
				.map(Picture::new)
				.distinct()
				.distinct(Picture::getTag)
				.sample(1, TimeUnit.SECONDS)
				.subscribe(System.out::println);
	}

	@Test
	public void sample_182() throws Exception {
		Observable
				.range(0, Integer.MAX_VALUE)
				.map(Picture::new)
				.window(1, TimeUnit.SECONDS)
				.flatMap(Observable::count)
				.subscribe(System.out::println);
	}

	@Test
	public void sample_192() throws Exception {
		Observable
				.range(0, Integer.MAX_VALUE)
				.map(Picture::new)
				.window(10, TimeUnit.SECONDS)
				.flatMap(Observable::distinct);
	}

	@Test
	public void sample_201() throws Exception {
		Observable<Incident> incidents = Observable.empty(); //...

		Observable<Boolean> danger = incidents
				.buffer(1, TimeUnit.SECONDS)
				.map((List<Incident> oneSecond) -> oneSecond
						.stream()
						.filter(Incident::isHIghPriority)
						.count() > 5);
	}

	@Test
	public void sample_213() throws Exception {
		Observable<Picture> fast = Observable
				.interval(10, MICROSECONDS)
				.map(Picture::new);
		Observable<Picture> slow = Observable
				.interval(11, MICROSECONDS)
				.map(Picture::new);

		Observable
				.zip(fast, slow, (f, s) -> f + " : " + s);

		Observable
				.zip(
						fast.onBackpressureDrop(),
						slow.onBackpressureDrop(),
						(f, s) -> f + " : " + s);
	}

}
