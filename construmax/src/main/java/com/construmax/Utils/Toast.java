package com.construmax.Utils;
import org.controlsfx.control.Notifications;

import com.construmax.App;

import javafx.geometry.Pos;

public class Toast {
    public static final void showToastSucess (String text) {
        Notifications.create()
        .title("Sucesso!")
        .text(text)
        .owner(App.getPrimaryStage())
        .position(Pos.BOTTOM_RIGHT)
        .hideAfter(javafx.util.Duration.seconds(3))
        .showInformation();
    }
}
