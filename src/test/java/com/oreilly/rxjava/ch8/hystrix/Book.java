package com.oreilly.rxjava.ch8.hystrix;

class Book {
}

class Rating {

	private final Book book;

	Rating(Book book) {
		this.book = book;
	}

	public Book getBook() {
		return book;
	}
}