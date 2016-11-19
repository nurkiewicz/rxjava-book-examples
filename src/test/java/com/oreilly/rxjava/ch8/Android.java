package com.oreilly.rxjava.ch8;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.oreilly.rxjava.ch8.rxandroid.AndroidSchedulers;
import com.oreilly.rxjava.ch8.rxbinding.RxTextView;
import com.oreilly.rxjava.ch8.rxbinding.RxView;
import com.oreilly.rxjava.ch8.rxbinding.TextViewAfterTextChangeEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

@Ignore
public class Android extends Activity {

	private final MeetupApi meetup = new ApiFactory().meetup();

	@Test
	public void sample_9() throws Exception {
		Button button = null;  //...
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				meetup
						.listCities(52.229841, 21.011736)
						.concatMapIterable(extractCities())
						.map(toCityName())
						.toList()
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(
								putOnListView(),
								displayError());
			}

			//...

		});
	}

	//Cities::getResults
	Func1<Cities, Iterable<City>> extractCities() {
		return new Func1<Cities, Iterable<City>>() {
			@Override
			public Iterable<City> call(Cities cities) {
				return cities.getResults();
			}
		};
	}

	//City::getCity
	Func1<City, String> toCityName() {
		return new Func1<City, String>() {
			@Override
			public String call(City city) {
				return city.getCity();
			}
		};
	}

	//cities -> listView.setAdapter(...)
	Action1<List<String>> putOnListView() {
		ListView listView = null; //...
		return new Action1<List<String>>() {
			@Override
			public void call(List<String> cities) {
				listView.setAdapter(new ArrayAdapter(Android.this, -1 /*R.layout.list*/, cities));
			}
		};
	}

	//throwable -> {...}
	Action1<Throwable> displayError() {
		return new Action1<Throwable>() {
			@Override
			public void call(Throwable throwable) {
				Log.e(TAG, "Error", throwable);
				Toast.makeText(Android.this, "Unable to load cities", Toast.LENGTH_SHORT).show();
			}
		};
	}

	@Test
	public void sample_98() throws Exception {
		Button button = new Button(null);
		RxView
				.clicks(button)
				.flatMap(c -> meetup.listCities(52.229841, 21.011736))
				.delay(2, TimeUnit.SECONDS)
				.concatMapIterable(extractCities())
				.map(toCityName())
				.toList()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						putOnListView(),
						displayError());
	}

	Func1<Void, Observable<Cities>> listCities(final double lat, final double lon) {
		return new Func1<Void, Observable<Cities>>() {
			@Override
			public Observable<Cities> call(Void aVoid) {
				return meetup.listCities(lat, lon);
			}
		};
	}

	@Test
	public void sample_121() throws Exception {
		EditText latText = null;//...
		EditText lonText = null;//...

		Observable<Double> latChanges = RxTextView
				.afterTextChangeEvents(latText)
				.flatMap(toDouble());
		Observable<Double> lonChanges = RxTextView
				.afterTextChangeEvents(lonText)
				.flatMap(toDouble());

		Observable<Cities> cities = Observable
				.combineLatest(latChanges, lonChanges, toPair())
				.debounce(1, TimeUnit.SECONDS)
				.flatMap(listCitiesNear());
	}

	Func1<TextViewAfterTextChangeEvent, Observable<Double>> toDouble() {
		return new Func1<TextViewAfterTextChangeEvent, Observable<Double>>() {
			@Override
			public Observable<Double> call(TextViewAfterTextChangeEvent e) {
				String s = e.editable().toString();
				try {
					return Observable.just(Double.parseDouble(s));
				} catch (NumberFormatException ex) {
					return Observable.empty();
				}
			}
		};
	}

	//return Pair::new
	Func2<Double, Double, Pair<Double, Double>> toPair() {
		return new Func2<Double, Double, Pair<Double, Double>>() {
			@Override
			public Pair<Double, Double> call(Double lat, Double lon) {
				return Pair.of(lat, lon);
			}
		};
	}

	//return latLon -> meetup.listCities(latLon.first, latLon.second)
	Func1<Pair<Double, Double>, Observable<Cities>> listCitiesNear() {
		return new Func1<Pair<Double, Double>, Observable<Cities>>() {
			@Override
			public Observable<Cities> call(Pair<Double, Double> latLon) {
				return meetup.listCities(latLon.getLeft(), latLon.getRight());
			}
		};
	}

}
