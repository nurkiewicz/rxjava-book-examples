package com.oreilly.rxjava.ch8.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class BlockingCmd extends HystrixCommand<String> {

	public BlockingCmd() {
		super(HystrixCommandGroupKey.Factory.asKey("SomeGroup"));
	}

	@Override
	protected String run() throws IOException {
		final URL url = new URL("http://www.example.com");
		try (InputStream input = url.openStream()) {
			return IOUtils.toString(input, StandardCharsets.UTF_8);
		}
	}

}
