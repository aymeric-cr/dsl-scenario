package fdit.gui.utils.imageButton;

import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;

import static javafx.scene.Cursor.DEFAULT;
import static javafx.scene.Cursor.HAND;

public class ImageButton extends ImageView {

    public ImageButton() {
        super();
        setPickOnBounds(true);
        setPreserveRatio(true);

        setOnMouseEntered(event -> {
            getScene().setCursor(HAND);
            final ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setContrast(0.9);
            colorAdjust.setBrightness(0.1);
            colorAdjust.setSaturation(0.2);
            setEffect(colorAdjust);
        });
        setOnMouseExited(event -> {
            getScene().setCursor(DEFAULT);
            setEffect(null);
        });
    }

    public void setToolTip(String toolTip) {
        Tooltip.install(this, new Tooltip(toolTip));
    }
}