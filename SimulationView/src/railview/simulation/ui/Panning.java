package railview.simulation.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;


public class Panning {


	private final EventHandlerManager handlerManager;

	private EventHandler<? super MouseEvent> mouseFilter = DEFAULT_FILTER;
	
	private final ValueAxis<?> xAxis;
	private final ValueAxis<?> yAxis;

	private boolean dragging = false;

	private boolean wasXAnimated;
	private boolean wasYAnimated;

	private double lastX;
	private double lastY;

	public Panning( XYChart<?, ?> chart ) {
		handlerManager = new EventHandlerManager( chart );
		xAxis = (ValueAxis<?>) chart.getXAxis();
		yAxis = (ValueAxis<?>) chart.getYAxis();

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

		wasXAnimated = xAxis.getAnimated();
		wasYAnimated = yAxis.getAnimated();

		xAxis.setAnimated( false );
		xAxis.setAutoRanging( false );
		yAxis.setAnimated( false );
		yAxis.setAutoRanging( false );

		dragging = true;
	}

	private void drag( MouseEvent event ) {
		if ( !dragging )
			return;

		double dX = ( event.getX() - lastX ) / -xAxis.getScale();
		double dY = ( event.getY() - lastY ) / -yAxis.getScale();
		lastX = event.getX();
		lastY = event.getY();

		xAxis.setAutoRanging(false);
		xAxis.setLowerBound( xAxis.getLowerBound() + dX );
		xAxis.setUpperBound( xAxis.getUpperBound() + dX );

		yAxis.setAutoRanging( false );
		yAxis.setLowerBound( yAxis.getLowerBound() + dY );
		yAxis.setUpperBound( yAxis.getUpperBound() + dY );
	}

	public EventHandler<? super MouseEvent> getMouseFilter() {
		return mouseFilter;
	}

	public void setMouseFilter( EventHandler<? super MouseEvent> mouseFilter ) {
		this.mouseFilter = mouseFilter;
	}

	public void start() {
		handlerManager.addAllHandlers();
	}

	public void stop() {
		handlerManager.removeAllHandlers();
		release();
	}
	
	private void release() {
		if ( !dragging )
			return;

		dragging = false;

		xAxis.setAnimated( wasXAnimated );
		yAxis.setAnimated( wasYAnimated );
	}
}

 class EventHandlerManager {
	private final Node target;
	private final List<Registration<? extends Event>> registrations;

	public EventHandlerManager( Node target ) {
		this.target = target;
		registrations = new ArrayList<Registration<? extends Event>>();
	}

	public Node getTarget() {
		return target;
	}

	public <T extends Event> void addEventHandler( boolean addImmediately, EventType<T> type,
	                                               EventHandler<? super T> handler ) {
		Registration<T> reg = new Registration<T>( type, handler );
		registrations.add( reg );
		if ( addImmediately ) {
			target.addEventHandler( type, handler );
			reg.setRegistered( true );
		}
	}


	public <T extends Event> void addEventHandler( EventType<T> type,
	                                               EventHandler<? super T> handler ) {
		addEventHandler( true, type, handler );
	}


	@SuppressWarnings( "unchecked" )
	public void addAllHandlers() {
		for ( Registration<?> registration : registrations ) {
			if ( !registration.isRegistered() ) {
				target.addEventHandler( (EventType) registration.getType(),
				                        (EventHandler) registration.getHandler() );
				registration.setRegistered( true );
			}
		}
	}

	@SuppressWarnings( "unchecked" )
	public void removeAllHandlers() {
		for ( Registration<?> registration : registrations ) {
			if ( registration.isRegistered() ) {
				target.removeEventHandler( (EventType) registration.getType(),
				                           (EventHandler) registration.getHandler() );
				registration.setRegistered( false );
			}
		}
	}

	private static class Registration<T extends Event> {
		private final EventType<T> type;
		private final EventHandler<? super T> handler;
		private boolean registered = false;

		public Registration( EventType<T> type, EventHandler<? super T> handler ) {
			if ( type == null )
			  throw new IllegalArgumentException( "type cannot be null" );
			if ( handler == null )
			  throw new IllegalArgumentException( "handler cannot be null" );

			this.type = type;
			this.handler = handler;
		}

		public EventType<T> getType() {
			return type;
		}

		public EventHandler<? super T> getHandler() {
			return handler;
		}

		public boolean isRegistered() {
			return registered;
		}

		public void setRegistered( boolean registered ) {
			this.registered = registered;
		}
	}
}

