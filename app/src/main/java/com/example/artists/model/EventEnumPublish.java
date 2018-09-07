package com.example.artists.model;

import rx.subjects.PublishSubject;

/**
 * A simple {@link PublishSubject} enum.
 * This enum is used for messaging between any parts of program by PublishSubjects
 * It is useful to create a new field for each new type of message
 * {@link EventEnumPublish#subscribe} to get the PublishSubject as is
 * to make a subscription for its events etc.
 * Use the {@link EventEnumPublish#publish} to emit new data to subscribers
 */
public enum EventEnumPublish {
    REPLACE_FRAGMENT;

    private PublishSubject<Object> publishSubject = PublishSubject.create();

    public PublishSubject<Object> subscribe() {
        return publishSubject;
    }

    public void publish(Object r) {
        publishSubject.onNext(r);
    }
}
