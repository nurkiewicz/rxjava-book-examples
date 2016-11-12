package com.oreilly.rxjava.ch5;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;


class HttpInitializer extends ChannelInitializer<SocketChannel> {

    private final HttpHandler httpHandler = new HttpHandler();

    @Override
    public void initChannel(SocketChannel ch) {
        ch
                .pipeline()
                .addLast(new HttpServerCodec())
                .addLast(httpHandler);
    }
}
