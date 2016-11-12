package com.oreilly.rxjava.ch7;

class Person {
}

class Health {
}

class Score {
	boolean isHigh() {
		return true;
	}
}

class InsuranceContract {
}

class Income {


	public Income(int i) {
	}

	static Income no() {
		return new Income(0);
	}

}
