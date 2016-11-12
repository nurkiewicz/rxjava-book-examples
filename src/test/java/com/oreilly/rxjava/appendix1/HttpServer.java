package com.oreilly.rxjava.appendix1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

abstract class HttpServer  {

    void run(int port) throws IOException {
        final ServerSocket serverSocket = new ServerSocket(port, 100);
        while (!Thread.currentThread().isInterrupted()) {
            final Socket client = serverSocket.accept();
            handle(new ClientConnection(client));
        }
    }

    abstract void handle(ClientConnection clientConnection);
}
