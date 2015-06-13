package railview.infrastructure;

import java.io.IOException;
import java.net.URL;

import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.Port;
import railapp.infrastructure.exception.NullIdException;
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
    
    private IInfrastructureServiceUtility serviceUtility;
    private NetworkPaneController networkPaneController;
    
    public InfrastructureElementViewController() {}
    
    public IInfrastructureServiceUtility getServiceUtility() {
    	return this.serviceUtility;
    }
    
    @FXML
    private void initialize() {
        // Initialize the person table with the two columns.
        idColumn.setCellValueFactory(cellData -> 
        	new SimpleStringProperty(cellData.getValue().getElement().getId().toString()));
        
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
    	this.serviceUtility = serviceUtility;
    	
    	setPortTable(serviceUtility);
    	this.networkPaneController.setInfrastructureServiceUtility(serviceUtility);
    }
    
	private void setPortTable(IInfrastructureServiceUtility serviceUtility) {
		ObservableList<Port> ports = FXCollections.observableArrayList();
		
		try {
			for (InfrastructureElement element : 
				serviceUtility.getInfrastructureElementService().findElements()) {
				for (Port port : element.getPorts()) {
					ports.add(port);
				}
			}
		} catch (NullIdException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		portTable.setItems(ports);
	}
	
//	private void drawInfrastructureElement(InfrastructureElement element) {
//		if (element.getPorts().size() == 2) {
//			drawLine(element.findPort(1).getCoordinate(), element.findPort(2).getCoordinate());
//		}
//		
//		if (element.getPorts().size() == 3) {
//			// Draw 1-2, Draw 3 - (1-2/2)
//			drawLine(element.findPort(1).getCoordinate(), element.findPort(2).getCoordinate());
//		}
//		
//		if (element.getPorts().size() == 4) {
//			// Draw 1-3, Draw 2-4
//			drawLine(element.findPort(1).getCoordinate(), element.findPort(3).getCoordinate());
//			drawLine(element.findPort(2).getCoordinate(), element.findPort(4).getCoordinate());
//		}
//	}
//	
//	private void drawLine(Coordinate c1, Coordinate c2) {
//		
//	}
	
}
