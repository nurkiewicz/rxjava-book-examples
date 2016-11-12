package com.oreilly.rxjava.ch1;

import java.util.function.Consumer;

class Callback {
	private Consumer<String> onResponse = x -> {};
	private Consumer<Exception> onFailure = x -> {};

	Callback onResponse(Consumer<String> consumer) {
		this.onResponse = consumer;
		return this;
	}

	Callback onFailure(Consumer<Exception> consumer) {
		this.onFailure = consumer;
		return this;
	}

	public Consumer<String> getOnResponse() {
		return onResponse;
	}

	public Consumer<Exception> getOnFailure() {
		return onFailure;
	}
}