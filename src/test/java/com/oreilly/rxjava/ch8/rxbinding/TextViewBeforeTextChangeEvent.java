package com.oreilly.rxjava.ch8.rxbinding;

import android.content.Context;
import android.widget.TextView;

import javax.annotation.Nonnull;

/**
 * A before text-change event on a view.
 * <p>
 * <strong>Warning:</strong> Instances keep a strong reference to the view. Operators that cache
 * instances have the potential to leak the associated {@link Context}.
 */
public final class TextViewBeforeTextChangeEvent extends ViewEvent<TextView> {
   @Nonnull
  public static TextViewBeforeTextChangeEvent create(@Nonnull TextView view,
      @Nonnull CharSequence text, int start, int count, int after) {
    return new TextViewBeforeTextChangeEvent(view, text, start, count, after);
  }

  private final CharSequence text;
  private final int start;
  private final int count;
  private final int after;

  private TextViewBeforeTextChangeEvent(@Nonnull TextView view, @Nonnull CharSequence text,
      int start, int count, int after) {
    super(view);
    this.text = text;
    this.start = start;
    this.count = count;
    this.after = after;
  }

  @Nonnull
  public CharSequence text() {
    return text;
  }

  public int start() {
    return start;
  }

  public int count() {
    return count;
  }

  public int after() {
    return after;
  }

  @Override public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof TextViewAfterTextChangeEvent)) return false;
    TextViewBeforeTextChangeEvent other = (TextViewBeforeTextChangeEvent) o;
    return other.view() == view()
        && text.equals(other.text)
        && start == other.start
        && count == other.count
        && after == other.after;
  }

  @Override public int hashCode() {
    int result = 17;
    result = result * 37 + view().hashCode();
    result = result * 37 + text.hashCode();
    result = result * 37 + start;
    result = result * 37 + count;
    result = result * 37 + after;
    return result;
  }

  @Override public String toString() {
    return "TextViewBeforeTextChangeEvent{text="
        + text
        + ", start="
        + start
        + ", count="
        + count
        + ", after="
        + after
        + ", view="
        + view()
        + '}';
  }

}