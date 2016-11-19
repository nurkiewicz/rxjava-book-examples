package com.oreilly.rxjava.ch8.rxbinding;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import com.oreilly.rxjava.ch8.rxandroid.MainThreadSubscription;
import rx.Observable;
import rx.Subscriber;

import static com.oreilly.rxjava.ch8.rxandroid.MainThreadSubscription.verifyMainThread;

final class TextViewTextOnSubscribe implements Observable.OnSubscribe<CharSequence> {
  final TextView view;

  TextViewTextOnSubscribe(TextView view) {
    this.view = view;
  }

  @Override public void call(final Subscriber<? super CharSequence> subscriber) {
    verifyMainThread();

    final TextWatcher watcher = new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!subscriber.isUnsubscribed()) {
          subscriber.onNext(s);
        }
      }

      @Override public void afterTextChanged(Editable s) {
      }
    };

    subscriber.add(new MainThreadSubscription() {
      @Override protected void onUnsubscribe() {
        view.removeTextChangedListener(watcher);
      }
    });

    view.addTextChangedListener(watcher);

    // Emit initial value.
    subscriber.onNext(view.getText());
  }
}