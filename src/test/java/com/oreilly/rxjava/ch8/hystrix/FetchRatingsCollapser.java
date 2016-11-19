package com.oreilly.rxjava.ch8.hystrix;

import com.netflix.hystrix.*;
import rx.functions.Func1;

import java.util.Collection;
import java.util.List;

import static com.netflix.hystrix.HystrixObservableCollapser.Setter.withCollapserKey;
import static java.util.stream.Collectors.toList;

public class FetchRatingsCollapser
	extends HystrixObservableCollapser<Book, Rating, Rating, Book> {

    private final Book book;

    public FetchRatingsCollapser(Book book) {
        super(withCollapserKey(HystrixCollapserKey.Factory.asKey("Books"))
                .andCollapserPropertiesDefaults(HystrixCollapserProperties.Setter()
                        .withTimerDelayInMilliseconds(20)
                        .withMaxRequestsInBatch(50)
                )
                .andScope(Scope.GLOBAL));
        this.book = book;
    }

    public Book getRequestArgument() {
        return book;
    }

    protected HystrixObservableCommand<Rating> createCommand(
    		Collection<HystrixCollapser.CollapsedRequest<Rating, Book>> requests) {
        List<Book> books = requests.stream()
                .map(c -> c.getArgument())
                .collect(toList());
        return new FetchManyRatings(books);
    }

    protected void onMissingResponse(HystrixCollapser.CollapsedRequest<Rating, Book> r) {
        r.setException(new RuntimeException("Not found for: " + r.getArgument()));
    }

    protected Func1<Book, Book> getRequestArgumentKeySelector() {
        return x -> x;
    }

    protected Func1<Rating, Rating> getBatchReturnTypeToResponseTypeMapper() {
        return x -> x;
    }

    protected Func1<Rating, Book> getBatchReturnTypeKeySelector() {
        return Rating::getBook;
    }

}
