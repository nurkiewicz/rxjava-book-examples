package com.oreilly.rxjava.ch4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.math.BigDecimal;

class RxGroceries {

    private static final Logger log = LoggerFactory.getLogger(RxGroceries.class);

    private final long start = System.currentTimeMillis();

    void log(Object label) {
        System.out.println(
                System.currentTimeMillis() - start + "\t| " +
                        Thread.currentThread().getName()   + "\t| " +
                        label);
    }


    Observable<BigDecimal> purchase(String productName, int quantity) {
        return Observable.fromCallable(() ->
            doPurchase(productName, quantity));
    }

    BigDecimal doPurchase(String productName, int quantity) {
        log("Purchasing " + quantity + " " + productName);
        //real logic here
        log("Done " + quantity + " " + productName);
        BigDecimal priceForProduct = BigDecimal.ONE;
        return priceForProduct;
    }

}
