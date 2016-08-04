package railview.simulation.ui.components;

import javafx.event.EventHandler;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class DraggableChart<X, Y> extends LineChart<X,Y>  {
	private final EventHandlerManager handlerManager;
	private EventHandler<? super MouseEvent> mouseFilter = DEFAULT_FILTER;

	private boolean wasXAnimated;
	private boolean wasYAnimated;

	private double lastX;
	private double lastY;

	private boolean dragging = false;

	public DraggableChart(Axis<X> xAxis, Axis<Y> yAxis) {
		super(xAxis, yAxis);

		handlerManager = new EventHandlerManager(this);

		handlerManager.addEventHandler( false, MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent mouseEvent ) {
				if ( passesFilter( mouseEvent ) )
					startDrag( mouseEvent );
			}

		} );

		handlerManager.addEventHandler( false, MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent mouseEvent ) {
				drag( mouseEvent );
			}
		} );

		handlerManager.addEventHandler( false, MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent mouseEvent ) {
				release();
			}
		} );

	}

	public static final EventHandler<MouseEvent> DEFAULT_FILTER = new EventHandler<MouseEvent>() {
		@Override
		public void handle( MouseEvent mouseEvent ) {
			if ( mouseEvent.getButton() != MouseButton.SECONDARY )
				mouseEvent.consume();
		}
	};

	public EventHandler<? super MouseEvent> getMouseFilter() {
		return mouseFilter;
	}

	public void setMouseFilter( EventHandler<? super MouseEvent> mouseFilter ) {
		this.mouseFilter = mouseFilter;
	}

	private boolean passesFilter( MouseEvent event ) {
		if ( mouseFilter != null ) {
			MouseEvent cloned = (MouseEvent) event.clone();
			mouseFilter.handle( cloned );
			if ( cloned.isConsumed() )
				return false;
		}

		return true;
	}

	private void startDrag( MouseEvent event ) {
		lastX = event.getX();
		lastY = event.getY();

		wasXAnimated = this.getXAxis().getAnimated();
		wasYAnimated = this.getYAxis().getAnimated();

		this.getXAxis().setAnimated( false );
		this.getXAxis().setAutoRanging( false );
		this.getYAxis().setAnimated( false );
		this.getYAxis().setAutoRanging( false );

		dragging = true;
	}

	private void drag( MouseEvent event ) {
		if ( !dragging )
			return;

		
		ValueAxis<?> theXAxis = (ValueAxis<?>) this.getXAxis();
		ValueAxis<?> theYAxis = (ValueAxis<?>) this.getYAxis();

		double dX = ( event.getX() - lastX ) / - theXAxis.getScale();
		double dY = ( event.getY() - lastY ) / - theYAxis.getScale();
		lastX = event.getX();
		lastY = event.getY();

		theXAxis.setAutoRanging(false);
		theXAxis.setLowerBound( theXAxis.getLowerBound() + dX );
		theXAxis.setUpperBound( theXAxis.getUpperBound() + dX );

		theYAxis.setAutoRanging( false );
		theYAxis.setLowerBound( theYAxis.getLowerBound() + dY );
		theYAxis.setUpperBound( theYAxis.getUpperBound() + dY );
	}

	public void startEventHandlers() {
		handlerManager.addAllHandlers();
	}

	public void stopEventHandlers() {
		handlerManager.removeAllHandlers();
		release();
	}

	private void release() {
		if ( !dragging )
			return;

		dragging = false;

		this.getXAxis().setAnimated( wasXAnimated );
		this.getYAxis().setAnimated( wasYAnimated );
	}
}




