package com.oreilly.rxjava.ch3;

import java.util.Arrays;
import java.util.List;

class Customer {

	List<Order> getOrders() {
		return Arrays.asList(new Order(), new Order());
	}

}
