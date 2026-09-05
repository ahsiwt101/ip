package milo.gui;

import java.io.IOException;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
        // Collections.reverse() briefly puts the same node at two positions
        // mid-swap, which Parent's children list rejects as a duplicate
        // child. Reversing a plain copy first, then replacing the children
        // in one atomic setAll(), avoids that intermediate state.
        ObservableList<Node> reversed = FXCollections.observableArrayList(getChildren());
        FXCollections.reverse(reversed);
        getChildren().setAll(reversed);
    }

    /**
     * Ties this bubble's maximum width to a fraction of the given width, so
     * long messages keep wrapping correctly as the window is resized
     * instead of being stuck at whatever width the window happened to be
     * when the message was created.
     *
     * @param containerWidth the width to track, typically the dialog
     *                        history's own width
     */
    public void bindMaxWidthTo(ReadOnlyDoubleProperty containerWidth) {
        dialog.maxWidthProperty().bind(containerWidth.multiply(0.7));
    }

    /**
     * Creates the dialog box for a message the user typed.
     *
     * @param text the user's message
     * @param img  the user's picture
     * @return the dialog box, aligned to the right
     */
    public static DialogBox getUserDialog(String text, Image img) {
        var db = new DialogBox(text, img);
        db.dialog.getStyleClass().add("user-bubble");
        return db;
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
        db.dialog.getStyleClass().add("milo-bubble");
        db.flip();
        return db;
    }
}
