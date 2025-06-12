package com.corevent.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class HBoxCellFactory<S> implements Callback<TableColumn<S, Void>, TableCell<S, Void>> {
    
    private final HBox hbox;
    
    public HBoxCellFactory(HBox hbox) {
        this.hbox = hbox;
    }
    
    @Override
    public TableCell<S, Void> call(TableColumn<S, Void> param) {
        return new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        };
    }
} 