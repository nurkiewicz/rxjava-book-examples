package com.oreilly.rxjava.appendix1;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

class ThreadPool extends HttpServer {

    private final ThreadPoolExecutor executor;

    public static void main(String[] args) throws IOException {
        new ThreadPool().run(8080);
    }

    public ThreadPool() {
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(1000);
        executor = new ThreadPoolExecutor(100, 100, 0L,
                                MILLISECONDS, workQueue,
                (r, ex) -> {
                    ((ClientConnection) r).serviceUnavailable();
                });
    }

    @Override
    void handle(ClientConnection clientConnection) {
        executor.execute(clientConnection);
    }

}
