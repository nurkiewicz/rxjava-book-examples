package com.oreilly.rxjava.ch6;

import java.util.List;

interface Repository {
    void store(Record record);
    void storeAll(List<Record> records);
}

class SomeRepository implements Repository {
    @Override
    public void store(Record record) {
    }

    @Override
    public void storeAll(List<Record> records) {
    }
}
