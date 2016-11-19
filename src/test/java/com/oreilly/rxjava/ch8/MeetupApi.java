package com.oreilly.rxjava.ch8;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;


public interface MeetupApi {

    @GET("/2/cities")
    Observable<Cities> listCities(
		@Query("lat") double lat,
		@Query("lon") double lon
    );

}
