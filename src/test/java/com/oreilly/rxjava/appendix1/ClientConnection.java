package com.oreilly.rxjava.appendix1;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class ClientConnection implements Runnable {

    public static final byte[] RESPONSE = (
            "HTTP/1.1 200 OK\r\n" +
            "Content-length: 2\r\n" +
            "\r\n" +
            "OK").getBytes();

    public static final byte[] SERVICE_UNAVAILABLE = (
            "HTTP/1.1 503 Service unavailable\r\n").getBytes();

    private final Socket client;

    ClientConnection(Socket client) {
        this.client = client;
    }

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                readFullRequest();
                client.getOutputStream().write(RESPONSE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            IOUtils.closeQuietly(client);
        }
    }

    private void readFullRequest() throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(client.getInputStream()));
        String line = reader.readLine();
        while (line != null && !line.isEmpty()) {
            line = reader.readLine();
        }
    }

    public void serviceUnavailable() {
        try {
            client.getOutputStream().write(SERVICE_UNAVAILABLE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
