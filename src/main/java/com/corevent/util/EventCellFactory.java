package com.corevent.util;

import com.corevent.entity.Event;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class EventCellFactory implements Callback<ListView<Event>, ListCell<Event>> {
    @Override
    public ListCell<Event> call(ListView<Event> param) {
        return new ListCell<Event>() {
            @Override
            protected void updateItem(Event event, boolean empty) {
                super.updateItem(event, empty);
                if (empty || event == null) {
                    setText(null);
                } else {
                    setText(event.getEventName());
                }
            }
        };
    }
} 