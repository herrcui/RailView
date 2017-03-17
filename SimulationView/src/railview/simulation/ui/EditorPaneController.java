package railview.simulation.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import py4j.GatewayServer;
import railapp.simulation.python.TimetableSimulationEntry;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class EditorPaneController {
	
	@FXML
	private TextArea editArea;
	
	@FXML
	private TextArea infoArea;
	
	private FileChooser fileChooser = new FileChooser();
	private File file;
	
	@FXML
	protected void onNew(ActionEvent event) {
		this.editArea.clear();
		this.file = null;
	}

	@FXML
	protected void onLoad(ActionEvent event) {
			this.file = fileChooser.showOpenDialog(null);
			if(this.file != null) {
				this.editArea.clear();
				BufferedReader bufferedReader = null;
				try {
					String currentLine;
					bufferedReader = new BufferedReader(new FileReader(this.file));
					while((currentLine = bufferedReader.readLine()) != null)
						editArea.appendText(currentLine + "\n");
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
	}
	
	@FXML
	protected void onSave(ActionEvent event) {
		String content = this.editArea.getText();
		FileChooser.ExtensionFilter extFilter = 
                new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);
		if(this.file != null) {
			try {
				if(!this.file.exists()) 
					this.file.createNewFile();
				FileWriter fileWriter = new FileWriter(this.file.getAbsoluteFile());
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(content);
				bufferedWriter.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		else {
			
			this.file = this.fileChooser.showSaveDialog(null);
			if(this.file != null) {
				try {
					if(!this.file.exists())
						this.file.createNewFile();
					FileWriter fileWriter = new FileWriter(this.file.getAbsoluteFile());
					BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
					bufferedWriter.write(content);
					bufferedWriter.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@FXML
	private void onSaveAs() {
		this.file = this.fileChooser.showSaveDialog(null);
		
		String content = this.editArea.getText();
		if(this.file != null) {
			Stage stage = (Stage) this.editArea.getScene().getWindow();
			stage.setTitle(this.file.getName() + " - jNotepad");
			try {
				if(!this.file.exists())
					this.file.createNewFile();
				
				FileWriter fileWriter = new FileWriter(this.file.getAbsoluteFile());
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(content);
				bufferedWriter.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	@FXML
	protected void onPlay(ActionEvent event) {
		infoArea.clear();
		infoArea.appendText("Here is the standard output of the command:\n");
		
		GatewayServer gatewayServer = new GatewayServer(new TimetableSimulationEntry());
        gatewayServer.start();
        
		try {			
			String s = "";
			ProcessBuilder pb = new ProcessBuilder(
					"python", this.file.getPath());
			Process p = pb.start();
			
			BufferedReader stdInput = new BufferedReader(new 
                InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new 
                InputStreamReader(p.getErrorStream()));

            // read the output from the command
            while ((s = stdInput.readLine()) != null) {
                infoArea.appendText(s);
                infoArea.appendText("\n");
            }
            
            // read any errors from the attempted command
            infoArea.appendText("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
            	infoArea.appendText(s);
            	infoArea.appendText("\n");
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		gatewayServer.shutdown();
	}
}


