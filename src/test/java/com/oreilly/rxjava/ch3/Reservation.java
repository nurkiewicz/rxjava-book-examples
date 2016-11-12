package com.oreilly.rxjava.ch3;

class Reservation {

    Reservation consume(ReservationEvent event) {
        //mutate myself
        return this;
    }

}
