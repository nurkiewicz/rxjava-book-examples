package com.oreilly.rxjava.ch5;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;

import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;

@Ignore
public class Chapter5 {

	@Test
	public void sample_9() throws Exception {
		Observable<ByteBuf> response = HttpClient
				.newClient("example.com", 80)
				.createGet("/")
				.flatMap(HttpClientResponse::getContent);
		response
				.map(bb -> bb.toString(UTF_8))
				.subscribe(System.out::println);
	}

	@Test
	public void sample_22() throws Exception {
		Observable<URL> sources = Observable.just(new URL("http://www.google.com"));

		Observable<ByteBuf> packets =
				sources
						.flatMap(url -> HttpClient
								.newClient(url.getHost(), url.getPort())
								.createGet(url.getPath()))
						.flatMap(HttpClientResponse::getContent);
	}

}
