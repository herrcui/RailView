package railview.simulation;

import java.io.IOException;
import java.net.URL;

import railapp.infrastructure.dto.Network;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.service.IRollingStockServiceUtility;
import railapp.simulation.SimulationManager;
import railapp.timetable.service.ITimetableServiceUtility;
import railview.railmodel.infrastructure.railsys7.InfrastructureReader;
import railview.railmodel.infrastructure.railsys7.RollingStockReader;
import railview.railmodel.infrastructure.railsys7.TimetableReader;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

public class DoSim extends Application {

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("DoSim");
		this.initRootLayout();
	}

	public static void main(String[] args) {
		infraServiceUtility = InfrastructureReader.getInstance().initialize();
		Network network = infraServiceUtility.getNetworkService().allNetworks().iterator().next();
		
		// Rollilngstock
		rollingStockServiceUtility = RollingStockReader.getInstance().initialize();
		
		// Timetable
		timeTableServiceUtility = TimetableReader.getInstance(
				infraServiceUtility, rollingStockServiceUtility, network).initialize();
		
		simulator = SimulationManager.getInstance(infraServiceUtility,
				rollingStockServiceUtility,
				timeTableServiceUtility);
		
		launch(args);
	}
	
	private void initRootLayout() {
		SimulationController controller = this.initializeSimulationController();
		
		if (this.rootLayout != null) {
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);

			controller.setInfrastructureServiceUtility(infraServiceUtility);
			controller.setSimulationManager(simulator);
			primaryStage.show();
			letterbox(scene, rootLayout);
		}
	}
	
	private void letterbox(final Scene scene, final AnchorPane rootLayout) {
	    final double initWidth  = scene.getWidth();
	    final double initHeight = scene.getHeight();
	    final double ratio      = initWidth / initHeight;

	    SceneSizeChangeListener sizeListener = new SceneSizeChangeListener(scene, ratio, initHeight, initWidth, rootLayout);
	    scene.widthProperty().addListener(sizeListener);
	    scene.heightProperty().addListener(sizeListener);
	  }

  private static class SceneSizeChangeListener implements ChangeListener<Number> {
	    private final Scene scene;
	    private final double ratio;
	    private final double initHeight;
	    private final double initWidth;
	    private final AnchorPane rootLayout;

	    public SceneSizeChangeListener(Scene scene, double ratio, double initHeight, double initWidth, AnchorPane rootLayout) {
	      this.scene = scene;
	      this.ratio = ratio;
	      this.initHeight = initHeight;
	      this.initWidth = initWidth;
	      this.rootLayout = rootLayout;
	    }

	    @Override
	    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
	      final double newWidth  = scene.getWidth();
	      final double newHeight = scene.getHeight();

	      double scaleFactor =
	          newWidth / newHeight > ratio
	              ? newHeight / initHeight
	              : newWidth / initWidth;

	      if (scaleFactor >= 1) {
	        Scale scale = new Scale(scaleFactor, scaleFactor);
	        scale.setPivotX(0);
	        scale.setPivotY(0);
	        scene.getRoot().getTransforms().setAll(scale);

	        rootLayout.setPrefWidth (newWidth  / scaleFactor);
	        rootLayout.setPrefHeight(newHeight / scaleFactor);
	      } else {
	    	  rootLayout.setPrefWidth (Math.max(initWidth,  newWidth));
	    	  rootLayout.setPrefHeight(Math.max(initHeight, newHeight));
	      }
	    }
	  }
	
	private SimulationController initializeSimulationController() {
		try {
			FXMLLoader loader = new FXMLLoader();
			URL location = SimulationController.class
					.getResource("SimulationViewer.fxml");
			loader.setLocation(location);

			this.rootLayout = (AnchorPane) loader.load();

			SimulationController controller = loader.getController();
			return controller;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Stage primaryStage;
	private AnchorPane rootLayout;

	private static SimulationManager simulator;
	private static IInfrastructureServiceUtility infraServiceUtility;
	private static IRollingStockServiceUtility rollingStockServiceUtility;
	private static ITimetableServiceUtility timeTableServiceUtility;
}
