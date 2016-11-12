package com.oreilly.rxjava.ch5;

import java.time.LocalDate;

class User {

	LocalDate getBirth() {
		return LocalDate.now().minusYears(30);
	}

}
