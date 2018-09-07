package com.example.artists.model;

import rx.subjects.BehaviorSubject;

/**
 * A simple {@link BehaviorSubject} enum.
 * This enum is used for messaging between any parts of program by BehaviorSubjects
 * It is useful to create a new field for each new type of message
 * {@link EventEnumBehavior#subscribe} to get the BehaviorSubject as is
 * to make a subscription for its events etc.
 * Use the {@link EventEnumBehavior#publish} to emit new data to subscribers
 */
public enum EventEnumBehavior {
    DOWNLOAD_FINISHED, PUBLISH_FILE, HANDLE_ARTISTS, DETAILS_FRAGMENT_INFO;

    private BehaviorSubject<Object> behaviorSubject = BehaviorSubject.create();

    public BehaviorSubject<Object> subscribe() {
        return behaviorSubject;
    }

    public void publish(Object r) {
        behaviorSubject.onNext(r);
    }

}
