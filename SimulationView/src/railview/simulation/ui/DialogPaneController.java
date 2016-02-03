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
    	
    }
    
    private void openInfrastructure() {
    	infraButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                    	
                    //	configure FileChooser(fileChooser);
                    //	File file = directoryChooser.showDialog(anchorPane.getScene().getWindows();
                        configureFileChooser(directoryChooser);
                        File file = directoryChooser.showDialog(anchorPane.getScene().getWindow());
                        if (file != null) {
                            openInfra(file);
                        }
                    }
                });
    	}
     
    private void openRollingstock() {
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
                });
    	}
    

    private static void configureFileChooser(DirectoryChooser directoryChooser){                           
    	directoryChooser.setTitle("Chose the directory");
        }
    
    private void openInfra(File directory) {
    		directory.getPath();    
    		textOne.setText(directory.getAbsolutePath());
 //   		String idStr = new File(directory.getPath()).getName();
 //           textOne.setText("\\"+idStr);
    }
    
    private void openRolling(File directory) {
		directory.getPath();    
		textTwo.setText(directory.getAbsolutePath());
}
    
    private void openTime(File directory) {
  		directory.getPath();    
  		textThree.setText(directory.getAbsolutePath());
  }
    
    
 /**   private void openInfra(File file) {
		file.getPath();    
        textOne.setText(file.getAbsolutePath());
    }
**/

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
    	openInfrastructure();
    	openRollingstock();
    	openTimetable();
    }
    
    

    DirectoryChooser directoryChooser = new DirectoryChooser();
//	FileChooser fileChooser = new FileChooser();
}
