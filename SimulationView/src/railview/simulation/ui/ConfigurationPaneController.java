package railview.simulation.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ConfigurationPaneController extends Stage implements Initializable{
	@FXML
	private AnchorPane configurationPaneRoot;
	
	@FXML
	private RadioButton defaultRB;
	
	@FXML
	private RadioButton externalRB;
	
	@FXML
	private Button externalFileButton;
	
	@FXML
	private Label fileNameLabel;
	
	@FXML
	private TextArea externalScript;
	
	@FXML
	private Button saveButton;
			
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		externalScript.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				saveButton.setDisable(false);
			}
		});
	}
	
	public ConfigurationPaneController() {
		setTitle("Configurations");

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ConfigurationPane.fxml"));
        fxmlLoader.setController(this); 

        fileChooser.setTitle("Choose external Python dispatching file ...");
        FileChooser.ExtensionFilter extFilter = 
                new FileChooser.ExtensionFilter("Python files (*.py)", "*.py");
        fileChooser.getExtensionFilters().add(extFilter);
        
        try
        {
            setScene(new Scene((Parent) fxmlLoader.load()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
	}
	
	@FXML
    private void onDefaultRB(ActionEvent event) {
		this.externalFileButton.setDisable(this.defaultRB.isSelected());
		this.externalRB.setSelected(! this.defaultRB.isSelected());
		this.fileNameLabel.setDisable(this.defaultRB.isSelected());
		this.externalScript.setDisable(this.defaultRB.isSelected());
    }
	
	@FXML
    private void onExternalRB(ActionEvent event) {
		this.defaultRB.setSelected(! this.externalRB.isSelected());
		this.externalFileButton.setDisable(this.defaultRB.isSelected());
		this.fileNameLabel.setDisable(this.defaultRB.isSelected());
		this.externalScript.setDisable(this.defaultRB.isSelected());
    }
	
	@FXML
	private void onSave(ActionEvent event) {
		String content = this.externalScript.getText();
		
		if(this.file != null) {
			try {
				if(!this.file.exists()) 
					this.file.createNewFile();
				FileWriter fileWriter = new FileWriter(this.file.getAbsoluteFile());
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(content);
				bufferedWriter.close();
				saveButton.setDisable(true);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@FXML
    private void onExternalFileButton(ActionEvent event) {
		this.file = fileChooser.showOpenDialog(configurationPaneRoot.getScene().getWindow());
		
        if (this.file != null) {
        	this.fileNameLabel.setText(file.getPath());
        	
        	this.externalScript.clear();
			BufferedReader bufferedReader = null;
			try {
				String currentLine;
				bufferedReader = new BufferedReader(new FileReader(file));
				while((currentLine = bufferedReader.readLine()) != null)
					externalScript.appendText(currentLine + "\n");
			} catch(Exception e) {
				e.printStackTrace();
			}
        }
    }
	
	private FileChooser fileChooser = new FileChooser();
	private File file = null;
}
