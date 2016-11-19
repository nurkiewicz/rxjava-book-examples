package com.oreilly.rxjava.ch8.rxbinding;

import android.view.View;
import rx.Observable;
import rx.functions.Action1;

import javax.annotation.Nonnull;

import static com.oreilly.rxjava.ch8.rxbinding.internal.Preconditions.checkNotNull;


/**
 * Static factory methods for creating {@linkplain Observable observables} and {@linkplain Action1
 * actions} for {@link View}.
 */
public final class RxView {

  /**
   * Create an observable which emits on {@code view} click events. The emitted value is
   * unspecified and should only be used as notification.
   * <p>
   * <em>Warning:</em> The created observable keeps a strong reference to {@code view}. Unsubscribe
   * to free this reference.
   * <p>
   * <em>Warning:</em> The created observable uses {@link View#setOnClickListener} to observe
   * clicks. Only one observable can be used for a view at a time.
   */
  @Nonnull
  public static Observable<Void> clicks(@Nonnull View view) {
    checkNotNull(view, "view == null");
    return Observable.create(new ViewClickOnSubscribe(view));
  }

}