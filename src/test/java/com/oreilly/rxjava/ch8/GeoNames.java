package com.oreilly.rxjava.ch8;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import rx.schedulers.Schedulers;

public interface GeoNames {

    Logger log = LoggerFactory.getLogger(GeoNames.class);

    default Observable<Integer> populationOf(String query) {
        return search(query)
                .concatMapIterable(SearchResult::getGeonames)
                .map(Geoname::getPopulation)
                .filter(p -> p != null)
                .singleOrDefault(0)
                .doOnError(th ->
                        log.warn("Falling back to 0 for {}", query, th))
                .onErrorReturn(th -> 0)
                .subscribeOn(Schedulers.io());
    }

    default Observable<SearchResult> search(String query) {
        return search(query, 1, "LONG", "some_user");
    }

    @GET("/searchJSON")
    Observable<SearchResult> search(
            @Query("q") String query,
            @Query("maxRows") int maxRows,
            @Query("style") String style,
            @Query("username") String username
    );

}
