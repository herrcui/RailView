package railview.swarmintelligence.ui;

import java.util.Collection;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import railapp.swarmintelligence.Swarm;
import railapp.units.Time;




public class SwarmSidebarController {
	
	@FXML
    private TableView<Swarm> swarmTable;
	
    @FXML
    private TableColumn<Swarm, String> idColumn;
    
    @FXML
    private TableColumn<Swarm, String> creationTimeColumn;
	
	
	public void updateSwarms(Collection<Swarm> swarms, Time time) {
		ObservableList<Swarm> data = FXCollections.observableArrayList();
		data.addAll(swarms);
		
		idColumn.setCellValueFactory(celldata -> new SimpleStringProperty(
				celldata.getValue().getId().toString()));
		creationTimeColumn.setCellValueFactory(celldata -> new SimpleStringProperty(
				celldata.getValue().getCreationTime().toString()));
		
		swarmTable.setItems(data);
	}
}
