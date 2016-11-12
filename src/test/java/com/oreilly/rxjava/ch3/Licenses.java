package com.oreilly.rxjava.ch3;

import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;

@Ignore
public class Licenses {

	Observable<CarPhoto> cars() {
		return Observable.just(new CarPhoto());
	}

	Observable<LicensePlate> recognize(CarPhoto photo) {
		return Observable.just(new LicensePlate());
	}

	@Test
	public void sample_100() throws Exception {
		Observable<CarPhoto> cars = cars();

		Observable<Observable<LicensePlate>> plates =
				cars.map(this::recognize);

		Observable<LicensePlate> plates2 =
				cars.flatMap(this::recognize);
	}


	Observable<LicensePlate> fastAlgo(CarPhoto photo) {
		//Fast but poor quality
		return Observable.just(new LicensePlate());
	}

	Observable<LicensePlate> preciseAlgo(CarPhoto photo) {
		//Precise but can be expensive
		return Observable.just(new LicensePlate());
	}

	Observable<LicensePlate> experimentalAlgo(CarPhoto photo) {
		//Unpredictable, running anyway
		return Observable.just(new LicensePlate());
	}

	@Test
	public void sample_317() throws Exception {
		CarPhoto photo = new CarPhoto();
		Observable<LicensePlate> all = Observable.merge(
				preciseAlgo(photo),
				fastAlgo(photo),
				experimentalAlgo(photo)
		);
	}

}
