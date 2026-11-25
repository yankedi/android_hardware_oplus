package com.oplus.osense.eventinfo;

import java.util.HashSet;
import java.util.Set;

public class EventConfig {

    private final HashSet<Integer> mEventSet;

    public EventConfig(HashSet<Integer> events) {
        mEventSet = events != null ? new HashSet<>(events) : new HashSet<>();
    }

    public Set<Integer> getEventSet() {
        return new HashSet<>(mEventSet);
    }

    public void setOsenseConfigSet(HashSet<String> hashSet) {}
}
