package com.construmax.Utils;
import org.controlsfx.control.Notifications;

import javafx.geometry.Pos;

public class Toast {
    public void showToastSucess (String text) {
        Notifications.create()
        .title("Sucesso!")
        .text(text)
        .position(Pos.BOTTOM_RIGHT)
        .hideAfter(javafx.util.Duration.seconds(3))
        .showConfirm();
    }
}
