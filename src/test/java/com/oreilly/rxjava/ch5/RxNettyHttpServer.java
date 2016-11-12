package com.oreilly.rxjava.ch5;

import io.reactivex.netty.protocol.http.server.HttpServer;
import rx.Observable;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

class RxNettyHttpServer {

    private static final Observable<String> RESPONSE_OK =
        Observable.just("OK");

    public static void main(String[] args) {
        HttpServer
            .newServer(8086)
            .start((req, resp) ->
                resp
                    .setHeader(CONTENT_LENGTH, 2)
                    .writeStringAndFlushOnEach(RESPONSE_OK)
            ).awaitShutdown();
    }

}
