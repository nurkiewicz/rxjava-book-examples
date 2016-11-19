package com.oreilly.rxjava.ch8.rxbinding;

import android.content.Context;
import android.view.View;

import javax.annotation.Nonnull;

import static com.oreilly.rxjava.ch8.rxbinding.internal.Preconditions.checkNotNull;

/**
 * A target view on which an event occurred (e.g., click).
 * <p>
 * <strong>Warning:</strong> Instances keep a strong reference to the view. Operators that cache
 * instances have the potential to leak the associated {@link Context}.
 */
public abstract class ViewEvent<T extends View> {
  private final T view;

  protected ViewEvent(@Nonnull T view) {
    this.view = checkNotNull(view, "view == null");
  }

  /** The view from which this event occurred. */
  @Nonnull public T view() {
    return view;
  }
}