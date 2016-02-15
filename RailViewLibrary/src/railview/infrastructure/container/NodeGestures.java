package railview.infrastructure.container;

import javafx.event.EventHandler;
import javafx.scene.input.ScrollEvent;
import railview.infrastructure.container.PannablePane;

class NodeGestures {

    private static final double MAX_SCALE = 100.0d;
    private static final double MIN_SCALE = .1d;
    double zoomFactor = 1.2;

    PannablePane canvas;

    public NodeGestures( PannablePane canvas) {
        this.canvas = canvas;

    }

    
    public EventHandler<ScrollEvent> getOnScrollEventHandler() {
        return onScrollEventHandler;
    }


    
    private EventHandler<ScrollEvent> onScrollEventHandler = new EventHandler<ScrollEvent>() {

        @Override
        public void handle(ScrollEvent event) {

            double scale = canvas.getScale();
            double oldScale = scale;

            if (event.getDeltaY() < 0)
                scale /= zoomFactor;
            else
                scale *= zoomFactor;

            scale = clamp( scale, MIN_SCALE, MAX_SCALE);

            double f = (scale / oldScale) - 1;

            double dx = (event.getX() - (canvas.getBoundsInParent().getWidth()/2 + canvas.getBoundsInParent().getMinX()));
            double dy = (event.getY() - (canvas.getBoundsInParent().getHeight()/2 + canvas.getBoundsInParent().getMinY()));

            canvas.setScale(scale);

            // note: pivot value must be untransformed, i. e. without scaling
            canvas.setPivot(f*dx, f*dy);

            event.consume();
        }
    };


    public static double clamp( double value, double min, double max) {

        if( Double.compare(value, min) < 0)
            return min;

        if( Double.compare(value, max) > 0)
            return max;

        return value;
    }

}
