package com.oreilly.rxjava.ch4;

import android.os.Handler;
import android.os.Looper;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.internal.schedulers.ScheduledAction;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

import java.util.concurrent.TimeUnit;

public final class SimplifiedHandlerScheduler extends Scheduler {

    @Override
    public Worker createWorker() {
        return new HandlerWorker();
    }

    static class HandlerWorker extends Worker {

        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public Subscription schedule(final Action0 action) {
            return schedule(action, 0, TimeUnit.MILLISECONDS);
        }

        private final CompositeSubscription compositeSubscription = new CompositeSubscription();

        @Override
        public void unsubscribe() {
            compositeSubscription.unsubscribe();
        }

        @Override
        public boolean isUnsubscribed() {
            return compositeSubscription.isUnsubscribed();
        }

        @Override
        public Subscription schedule(Action0 action, long delayTime, TimeUnit unit) {
            if (compositeSubscription.isUnsubscribed()) {
                return Subscriptions.unsubscribed();
            }

            final ScheduledAction scheduledAction = new ScheduledAction(action);
            scheduledAction.addParent(compositeSubscription);
            compositeSubscription.add(scheduledAction);

            handler.postDelayed(scheduledAction, unit.toMillis(delayTime));

            scheduledAction.add(Subscriptions.create(() ->
                    handler.removeCallbacks(scheduledAction)));

            return scheduledAction;
        }
    }
}
