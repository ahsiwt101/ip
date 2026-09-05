package milo.gui;

import java.io.IOException;
import java.util.Collections;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * A single chat bubble: a message next to a speaker's picture. Loaded from
 * {@code DialogBox.fxml}, with the layout mirrored for Milo's replies so
 * user and bot messages sit on opposite sides of the window.
 */
public class DialogBox extends HBox {
    @FXML
    private Label dialog;

    @FXML
    private ImageView displayPicture;

    private DialogBox(String text, Image img) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(DialogBox.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Could not load DialogBox.fxml", e);
        }

        dialog.setText(text);
        displayPicture.setImage(img);
    }

    /**
     * Mirrors this dialog box so its picture sits on the left and its text
     * on the right, distinguishing Milo's replies from the user's messages.
     */
    private void flip() {
        setAlignment(Pos.TOP_LEFT);
        Collections.reverse(getChildren());
    }

    /**
     * Creates the dialog box for a message the user typed.
     *
     * @param text the user's message
     * @param img  the user's picture
     * @return the dialog box, aligned to the right
     */
    public static DialogBox getUserDialog(String text, Image img) {
        return new DialogBox(text, img);
    }

    /**
     * Creates the dialog box for one of Milo's replies.
     *
     * @param text Milo's reply
     * @param img  Milo's picture
     * @return the dialog box, mirrored to sit on the left
     */
    public static DialogBox getMiloDialog(String text, Image img) {
        var db = new DialogBox(text, img);
        db.flip();
        return db;
    }
}
