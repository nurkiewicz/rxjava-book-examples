package com.oreilly.rxjava.ch8;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

class ApiFactory {

	GeoNames geoNames() {
		return new Retrofit.Builder()
				.baseUrl("http://api.geonames.org")
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.addConverterFactory(JacksonConverterFactory.create(objectMapper()))
				.build()
				.create(GeoNames.class);
	}

	MeetupApi meetup() {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl("https://api.meetup.com/")
				.addCallAdapterFactory(
						RxJavaCallAdapterFactory.create())
				.addConverterFactory(
						JacksonConverterFactory.create(objectMapper()))
				.build();
		return retrofit.create(MeetupApi.class);
	}

	private ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setPropertyNamingStrategy(
				PropertyNamingStrategy.SNAKE_CASE);
		objectMapper.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return objectMapper;
	}

}
