package railview.infrastructure.container;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;

/**
 * The canvas which holds all of the nodes of the application.
 */
public class PannablePane extends Pane {

    Scale scaleTransform;

    DoubleProperty myScale = new SimpleDoubleProperty(1.0);
    
    public PannablePane() {

        scaleXProperty().bind(myScale);
        scaleYProperty().bind(myScale);
    }
    
    public double getScale() {
        return myScale.get();
    }

    public void setScale( double scale) {
        myScale.set(scale);
    }

    public void setPivot( double x, double y) {
        setTranslateX(getTranslateX()-x);
        setTranslateY(getTranslateY()-y);
    }
}
