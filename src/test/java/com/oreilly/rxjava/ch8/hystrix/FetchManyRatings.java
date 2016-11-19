package com.oreilly.rxjava.ch8.hystrix;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import rx.Observable;

import java.util.Collection;

class FetchManyRatings extends HystrixObservableCommand<Rating> {

    private final Collection<Book> books;

    protected FetchManyRatings(Collection<Book> books) {
        super(HystrixCommandGroupKey.Factory.asKey("Books"));
        this.books = books;
    }

    @Override
    protected Observable<Rating> construct() {
        return fetchManyRatings(books);
    }

    private Observable<Rating> fetchManyRatings(Collection<Book> books) {
        return Observable
                .from(books)
                .map(Rating::new);
    }

}
