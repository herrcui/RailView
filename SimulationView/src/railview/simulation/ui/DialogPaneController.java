package railview.simulation.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class DialogPaneController extends Stage implements Initializable
{
	@FXML
	private AnchorPane anchorPane;
	
    @FXML
    private Button okButton;
    
    @FXML
    private Button infraButton;
    
    @FXML
    private Button rollingstockButton;
    
    @FXML
    private Button timetableButton;
  
    @FXML
    private TextField textOne;
    
    @FXML
    private TextField textTwo;
    
    @FXML
    private TextField textThree;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Button applyButton;
    
    @FXML
    private Button rootButton;
    
    private Path infraPath;
    private Path rollingStockPath;
    private Path timetablePath;

    public DialogPaneController(Parent parent)
    {
        setTitle("Filepaths");

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Dialog.fxml"));
        fxmlLoader.setController(this); 

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
    private void onCancelButtonAction(ActionEvent event)
    {
        close();
    }
    
    @FXML
    private void onApplyButtonAction(ActionEvent event)
    {
    	close();
    }
    
    public Path getInfrastructurePath() {
    	return this.infraPath;
    }
    
    public Path getRollingStockPath() {
    	return this.rollingStockPath;
    }
    
    public Path getTimeTablePath() {
    	return this.timetablePath;
    }
    
    private void openInfrastructure() {
    	
    	rootButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = directoryChooser.showDialog(anchorPane.getScene().getWindow());
                        directoryChooser.setInitialDirectory(file);
                    }
                });
    	
    	infraButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        configureFileChooser(directoryChooser);
                        File file = directoryChooser.showDialog(anchorPane.getScene().getWindow());
                        if (file != null) {
                            openInfra(file);
                        }
                    }
                });
    	}
     
    private void openRollingstock() {    	
    	
    	rootButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = directoryChooser.showDialog(anchorPane.getScene().getWindow());
                        directoryChooser.setInitialDirectory(file);
                    }
                });
    	
    	rollingstockButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        configureFileChooser(directoryChooser);
                        File file = directoryChooser.showDialog(anchorPane.getScene().getWindow());
                        if (file != null) {
                            openRolling(file);
                        }
                    }
                });
    	}
    
    private void openTimetable() {
    	
    	rootButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = directoryChooser.showDialog(anchorPane.getScene().getWindow());
                        directoryChooser.setInitialDirectory(file);
                    }
                });
    	
    	timetableButton.setOnAction(
           new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                    configureFileChooser(directoryChooser);
                    File file = directoryChooser.showDialog(anchorPane.getScene().getWindow());
                    if (file != null) {
                        openTime(file);
                    }
                }
           }
       );
    }
    

    private static void configureFileChooser(DirectoryChooser directoryChooser){                           
    	directoryChooser.setTitle("Chose the directory");
    }
    
    private void openInfra(File directory) {
		this.infraPath = directory.toPath();   
		textOne.setText(directory.getAbsolutePath());
    }
    
    private void openRolling(File directory) {
    	this.rollingStockPath = directory.toPath();   
		textTwo.setText(directory.getAbsolutePath());
    }
    
    private void openTime(File directory) {
    	this.timetablePath = directory.toPath(); 
  		textThree.setText(directory.getAbsolutePath());
    }
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
    	openInfrastructure();
    	openRollingstock();
    	openTimetable();
    }
    
    DirectoryChooser directoryChooser = new DirectoryChooser();
}
