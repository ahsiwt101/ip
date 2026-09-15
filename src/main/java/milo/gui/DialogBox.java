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
import javafx.scene.shape.Circle;

/**
 * A single chat bubble in the dialog history.
 * <p>
 * The two sides are deliberately not symmetric, because the conversation is
 * not between two people: Milo's replies carry its avatar and sit on the
 * left, while the user's messages are bare and sit on the right. The user
 * already knows which messages are theirs, so a second avatar would cost
 * width on every row and tell them nothing.
 */
public class DialogBox extends HBox {
    /** Radius of Milo's avatar, also half its fitted width and height. */
    private static final double AVATAR_RADIUS = 16.0;

    /** Share of the history's width a reply may occupy before wrapping. */
    private static final double MILO_WIDTH_FRACTION = 0.88;

    /** Share of the history's width a user message may occupy. */
    private static final double USER_WIDTH_FRACTION = 0.72;

    @FXML
    private Label dialog;

    @FXML
    private ImageView displayPicture;

    /** How much of the history's width this particular bubble may use. */
    private double widthFraction = MILO_WIDTH_FRACTION;

    private DialogBox(String text) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(DialogBox.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Could not load DialogBox.fxml", e);
        }

        dialog.setText(text);
    }

    /**
     * Shows the given picture as this bubble's avatar, clipped to a circle so
     * it reads as a face rather than as a pasted-on square.
     *
     * @param img the picture to show
     */
    private void showAvatar(Image img) {
        displayPicture.setImage(img);
        displayPicture.setFitWidth(AVATAR_RADIUS * 2);
        displayPicture.setFitHeight(AVATAR_RADIUS * 2);
        displayPicture.setClip(new Circle(AVATAR_RADIUS, AVATAR_RADIUS, AVATAR_RADIUS));
    }

    /** Drops the avatar entirely, leaving the message to use the full row. */
    private void removeAvatar() {
        getChildren().remove(displayPicture);
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
     * Ties this bubble's maximum width to a share of the given width, so long
     * messages keep wrapping correctly as the window is resized instead of
     * being stuck at whatever width the window happened to be when the
     * message was created. Milo's replies are allowed more of the row than
     * the user's messages, since they are the ones that run long.
     *
     * @param containerWidth the width to track, typically the dialog
     *                        history's own width
     */
    public void bindMaxWidthTo(ReadOnlyDoubleProperty containerWidth) {
        dialog.maxWidthProperty().bind(containerWidth.multiply(widthFraction));
    }

    /**
     * Creates the dialog box for a message the user typed.
     *
     * @param text the user's message
     * @return the dialog box, aligned to the right and carrying no avatar
     */
    public static DialogBox getUserDialog(String text) {
        DialogBox db = new DialogBox(text);
        db.removeAvatar();
        db.setAlignment(Pos.TOP_RIGHT);
        db.widthFraction = USER_WIDTH_FRACTION;
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
        DialogBox db = new DialogBox(text);
        db.showAvatar(img);
        db.flip();
        db.dialog.getStyleClass().add("milo-bubble");
        return db;
    }

    /**
     * Creates the dialog box for a reply Milo could not carry out. Styled
     * apart from an ordinary reply so a mistyped command is noticed rather
     * than scrolling past looking like a normal answer.
     *
     * @param text the explanation of what went wrong
     * @param img  Milo's picture
     * @return the dialog box, mirrored to sit on the left
     */
    public static DialogBox getErrorDialog(String text, Image img) {
        DialogBox db = new DialogBox(text);
        db.showAvatar(img);
        db.flip();
        db.dialog.getStyleClass().add("error-bubble");
        return db;
    }
}
