package com.oreilly.rxjava.ch8.rxandroid;

import android.app.Activity;
import android.os.Bundle;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    private final byte[] blob = new byte[32 * 1024 * 1024];

    private final CompositeSubscription allSubscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //...
        Subscription subscription = Observable
                .interval(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(x -> {
//                    text.setText(Long.toString(x));
                });
        allSubscriptions.add(subscription);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        allSubscriptions.unsubscribe();
    }

}
