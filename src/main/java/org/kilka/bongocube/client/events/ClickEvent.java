package org.kilka.bongocube.client.events;

import java.util.ArrayList;
import java.util.List;

public class ClickEvent {
    public interface TapListener {
        void keyWasTapped();
    }

    private final List<TapListener> listeners = new ArrayList<TapListener>();

    public void addListener(TapListener toAdd) {
        listeners.add(toAdd);
    }

    public void tap() {
        for (TapListener tapers : listeners)
            tapers.keyWasTapped();
    }
}
