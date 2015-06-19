package railview.infrastructure.editor;

import java.io.IOException;
import java.net.URL;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railview.infrastructure.container.NetworkPaneController;

public class InfrastructureEditorController {

	final double SCALE_DELTA = 1.1;
	@FXML
	private HBox networkPaneRoot;

	@FXML
	public void initialize() {
		try {
			FXMLLoader loader = new FXMLLoader();
			URL location = NetworkPaneController.class
					.getResource("NetworkPane.fxml");
			loader.setLocation(location);
			StackPane networkPane = (StackPane) loader.load();
			this.networkPaneController = loader.getController();
			this.networkPaneRoot.getChildren().add(networkPane);
			// StackPane.setAlignment(networkPaneRoot, Pos.CENTER);
			Scrollevent();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void Scrollevent() {
		networkPaneRoot.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				event.consume();

				if (event.getDeltaY() == 0) {
					return;
				}

				double scaleFactor = (event.getDeltaY() > 0) ? SCALE_DELTA
						: 1 / SCALE_DELTA;

				networkPaneRoot.setScaleX(networkPaneRoot.getScaleX()
						* scaleFactor);
				networkPaneRoot.setScaleY(networkPaneRoot.getScaleY()
						* scaleFactor);
			}
		});

	}

	public NetworkPaneController getNetworkPaneController() {
		return this.networkPaneController;
	}

	public void setInfrastructureServiceUtility(
			IInfrastructureServiceUtility serviceUtility) {
		this.networkPaneController
				.setInfrastructureServiceUtility(serviceUtility);
	}

	private NetworkPaneController networkPaneController;
}
