package com.oreilly.rxjava.ch8.rxbinding;

import android.view.KeyEvent;
import android.widget.TextView;

import javax.annotation.Nullable;

public final class TextViewEditorActionEvent extends ViewEvent<TextView> {

  public static TextViewEditorActionEvent create( TextView view, int actionId,
      @Nullable KeyEvent keyEvent) {
    return new TextViewEditorActionEvent(view, actionId, keyEvent);
  }

  private final int actionId;
  @Nullable private final KeyEvent keyEvent;

  private TextViewEditorActionEvent( TextView view, int actionId,
      @Nullable KeyEvent keyEvent) {
    super(view);
    this.actionId = actionId;
    this.keyEvent = keyEvent;
  }

  public int actionId() {
    return actionId;
  }

  @Nullable public KeyEvent keyEvent() {
    return keyEvent;
  }

  @Override public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof TextViewEditorActionEvent)) return false;
    TextViewEditorActionEvent other = (TextViewEditorActionEvent) o;
    return other.view() == view()
        && other.actionId == actionId
        && (other.keyEvent != null ? other.keyEvent.equals(keyEvent) : keyEvent == null);
  }

  @Override public int hashCode() {
    int result = 17;
    result = result * 37 + view().hashCode();
    result = result * 37 + actionId;
    result = result * 37 + (keyEvent != null ? keyEvent.hashCode() : 0);
    return result;
  }

  @Override public String toString() {
    return "TextViewEditorActionEvent{view="
        + view()
        + ", actionId="
        + actionId
        + ", keyEvent="
        + keyEvent
        + '}';
  }
}