package com.oreilly.rxjava.ch3;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

final class OperatorMap<T, R> implements Observable.Operator<R, T> {

    private final Func1<T, R> transformer;

    public OperatorMap(Func1<T, R> transformer) {
        this.transformer = transformer;
    }

    @Override
    public Subscriber<? super T> call(Subscriber<? super R> child) {
        return new Subscriber<T>(child) {

            @Override
            public void onCompleted() {
                child.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                child.onError(e);
            }

            @Override
            public void onNext(T t) {
                try {
                    child.onNext(transformer.call(t));
                } catch (Exception e) {
                    onError(e);
                }
            }
        };
    }

}
