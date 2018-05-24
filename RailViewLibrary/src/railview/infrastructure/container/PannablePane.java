package railview.infrastructure.container;

import javafx.scene.layout.Pane;

/**
 * It's the pannable canvas which holds all of the nodes of the application. 
 */
public class PannablePane extends Pane {
    public PannablePane() {
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
