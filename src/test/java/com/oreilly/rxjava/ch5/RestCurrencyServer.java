package com.oreilly.rxjava.ch5;

import io.reactivex.netty.protocol.http.server.HttpServer;
import rx.Observable;

import java.math.BigDecimal;

class RestCurrencyServer {

    private static final BigDecimal RATE = new BigDecimal("1.06448");

    public static void main(final String[] args) {
        HttpServer
                .newServer(8080)
                .start((req, resp) -> {
                    String amountStr = req.getDecodedPath().substring(1);
                    BigDecimal amount = new BigDecimal(amountStr);
                    Observable<String> response = Observable
                            .just(amount)
                            .map(eur -> eur.multiply(RATE))
                            .map(usd -> 
                                    "{\"EUR\": " + amount + ", " +
                                     "\"USD\": " + usd + "}");
                    return resp.writeString(response);
                })
                .awaitShutdown();
    }
}
