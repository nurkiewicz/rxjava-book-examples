package com.oreilly.rxjava.ch8;

class Picture {
    private final byte[] blob = new byte[128 * 1024];
    private final long tag;

    Picture(long tag) { this.tag = tag; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Picture)) return false;
        Picture picture = (Picture) o;
        return tag == picture.tag;
    }

    long getTag() {
        return tag;
    }

    @Override
    public int hashCode() {
        return (int) (tag ^ (tag >>> 32));
    }

    @Override
    public String toString() {
        return Long.toString(tag);
    }
}
