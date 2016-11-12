package com.oreilly.rxjava.ch4;

import rx.Observable;

import java.util.Arrays;
import java.util.List;

import static rx.Observable.defer;
import static rx.Observable.from;

class PersonDao {

    private static final int PAGE_SIZE = 10;

    Observable<Person> allPeople(int initialPage) {
        return defer(() -> from(listPeople(initialPage)))
                .concatWith(defer(() ->
                        allPeople(initialPage + 1)));
    }

    void allPeople() {
        Observable<Person> allPages = Observable
                .range(0, Integer.MAX_VALUE)
                .map(this::listPeople)
                .takeWhile(list -> !list.isEmpty())
                .concatMap(Observable::from);
    }

    List<Person> listPeople() {
        return query("SELECT * FROM PEOPLE");
    }

    List<Person> listPeople(int initialPage) {
        return query("SELECT * FROM PEOPLE OFFSET ? MAX ?", initialPage * 10, PAGE_SIZE);
    }

    Observable<Person> listPeople2() {
        final List<Person> people = query("SELECT * FROM PEOPLE");
        return Observable.from(people);
    }

    public Observable<Person> listPeople3() {
        return defer(() ->
                Observable.from(query("SELECT * FROM PEOPLE")));
    }

    private List<Person> query(String sql, Object... args) {
        //...
        return Arrays.asList(new Person(), new Person());
    }

}
