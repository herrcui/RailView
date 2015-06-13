package railview.infrastructure.container;

import java.util.Collection;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.exception.NullIdException;
import railapp.infrastructure.service.IInfrastructureServiceUtility;

public class NetworkPaneController {
	@FXML
    private Label infraLabel;
	
	public void setInfrastructureServiceUtility(IInfrastructureServiceUtility serviceUtility) {
		this.serviceUtility = serviceUtility;
		try {
			int num = this.serviceUtility.getInfrastructureElementService().findElements().size();
			this.infraLabel.setText("number of elements: " + num);
		} catch (NullIdException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@FXML
    private void initialize() {
		
	}
	
	public Group buildGroup(Collection<InfrastructureElement> elements) {
		Group group = new Group();
		for (InfrastructureElement element : elements) {
			setInfrastructureElement(group, element);
		}
		return group;
	}
	
	private void setInfrastructureElement(Group group, InfrastructureElement elememt) {
		// if 2 Ports
		// if 3 Ports
		// if 4 Ports
	}
	
	private IInfrastructureServiceUtility serviceUtility;
}
