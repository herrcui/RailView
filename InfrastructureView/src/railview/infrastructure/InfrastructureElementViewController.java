package railview.infrastructure;

import java.io.IOException;
import java.net.URL;

import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.Port;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railview.infrastructure.container.NetworkPaneController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class InfrastructureElementViewController {
	@FXML
    private TableView<Port> portTable;
    @FXML
    private TableColumn<Port, String> idColumn;
    @FXML
    private TableColumn<Port, String> portColumn;
    @FXML
    private TableColumn<Port, String> xColumn;
    @FXML
    private TableColumn<Port, String> yColumn;
    @FXML
    private AnchorPane networkPaneRoot;
    
    private NetworkPaneController networkPaneController;
    
    public InfrastructureElementViewController() {}
    
    @FXML
    private void initialize() {
        // Initialize the person table with the two columns.
        idColumn.setCellValueFactory(cellData -> 
        	new SimpleStringProperty(cellData.getValue().getInfrastructureElement().getId().toString()));
        
        portColumn.setCellValueFactory(cellData -> 
    		new SimpleStringProperty(Integer.toString(cellData.getValue().getNumber())));
        
        xColumn.setCellValueFactory(cellData -> 
			new SimpleStringProperty(Double.toString(cellData.getValue().getCoordinate().getX())));
        
        yColumn.setCellValueFactory(cellData -> 
			new SimpleStringProperty(Double.toString(cellData.getValue().getCoordinate().getY())));
        
		try {
            FXMLLoader loader = new FXMLLoader();
            URL location = NetworkPaneController.class.getResource("NetworkPane.fxml");
            loader.setLocation(location);
            StackPane networkPane = (StackPane) loader.load();
            
            this.networkPaneController = loader.getController();
            this.networkPaneRoot.getChildren().add(networkPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setInfrastructureServiceUtility(IInfrastructureServiceUtility serviceUtility) {  	
    	setPortTable(serviceUtility);
    	this.networkPaneController.setInfrastructureServiceUtility(serviceUtility);
    }
    
	private void setPortTable(IInfrastructureServiceUtility serviceUtility) {
		ObservableList<Port> ports = FXCollections.observableArrayList();
		
		for (InfrastructureElement element : 
			serviceUtility.getInfrastructureElementService().findElements()) {
			for (Port port : element.getPorts()) {
				ports.add(port);
			}
		}
				
		portTable.setItems(ports);
	}	
}
