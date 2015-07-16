package railview.infrastructure.container;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import railview.infrastructure.container.DragContext;
import railview.infrastructure.container.PannablePane;

class NodeGestures {

    private static final double MAX_SCALE = 100.0d;
    private static final double MIN_SCALE = .1d;
    private DragContext nodeDragContext = new DragContext();

    PannablePane canvas;

    public NodeGestures( PannablePane canvas) {
        this.canvas = canvas;

    }

    public EventHandler<MouseEvent> getOnMousePressedEventHandler() {
        return onMousePressedEventHandler;
    }

    public EventHandler<MouseEvent> getOnMouseDraggedEventHandler() {
        return onMouseDraggedEventHandler;
    }
    
    public EventHandler<ScrollEvent> getOnScrollEventHandler() {
        return onScrollEventHandler;
    }

    private EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

        public void handle(MouseEvent event) {

            // left mouse button => dragging
            if( !event.isPrimaryButtonDown())
                return;

            nodeDragContext.mouseAnchorX = event.getSceneX();
            nodeDragContext.mouseAnchorY = event.getSceneY();

            Node node = (Node) event.getSource();

            nodeDragContext.translateAnchorX = node.getTranslateX();
            nodeDragContext.translateAnchorY = node.getTranslateY();

        }

    };

    private EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {

            // left mouse button => dragging
            if( !event.isPrimaryButtonDown())
                return;

            double scale = canvas.getScale();

            Node node = (Node) event.getSource();

            node.setTranslateX(nodeDragContext.translateAnchorX + (( event.getSceneX() - nodeDragContext.mouseAnchorX) / scale));
            node.setTranslateY(nodeDragContext.translateAnchorY + (( event.getSceneY() - nodeDragContext.mouseAnchorY) / scale));

            event.consume();

        }
    };
    
    private EventHandler<ScrollEvent> onScrollEventHandler = new EventHandler<ScrollEvent>() {

        @Override
        public void handle(ScrollEvent event) {

            double delta = 1.2;

            double scale = canvas.getScale(); // currently we only use Y, same value is used for X
            double oldScale = scale;

            if (event.getDeltaY() < 0)
                scale -= delta;
            else
                scale += delta;

            if (scale <= MIN_SCALE) {
                scale = MIN_SCALE;
            } else if (scale >= MAX_SCALE) {
                scale = MAX_SCALE;
            }

            // pivot value must be untransformed, i. e. without scaling
            canvas.setPivot( 
                    ((event.getSceneX() - canvas.getBoundsInParent().getMinX()) / oldScale),
                    ((event.getSceneY() - canvas.getBoundsInParent().getMinY()) / oldScale)
                    );

            canvas.setScale( scale);

            System.out.println( "new pivot x: " + canvas.scaleTransform.getPivotX() + "/" + canvas.scaleTransform.getPivotY() + ", new scale: " + scale);
            System.out.println( "bounds: " + canvas.getBoundsInParent());       

            event.consume();

        }
    };
}
