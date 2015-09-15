package railview.infrastructure.container;

import javafx.scene.layout.Pane;

/**
 * The canvas which holds all of the nodes of the application.
 */
public class PannablePane extends Pane {
    public PannablePane() {
        this.setScale(1);
    }
    
    public double getScale() {
        return this.getScaleX();
    }

    public void setScale(double scale) {
        this.setScaleX(scale);
        this.setScaleY(scale);
    }

    public void setPivot(double x, double y) {
        setTranslateX(getTranslateX() - x);
        setTranslateY(getTranslateY() - y);
    }
}
