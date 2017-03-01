package railview.simulation.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class EditorPaneController {
	
	@FXML
	private TextArea textArea;
	
	private FileChooser fileChooser = new FileChooser();
	private File file;
	
	
	@FXML
	protected void onNew(ActionEvent event) {
		this.textArea.clear();
		this.file = null;
	}

	@FXML
	protected void onLoad(ActionEvent event) {
			this.file = fileChooser.showOpenDialog(null);
			if(this.file != null) {
				this.textArea.clear();
				BufferedReader bufferedReader = null;
				try {
					String currentLine;
					bufferedReader = new BufferedReader(new FileReader(this.file));
					while((currentLine = bufferedReader.readLine()) != null)
						textArea.appendText(currentLine + "\n");
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
	}
	
	@FXML
	protected void onSave(ActionEvent event) {
		String content = this.textArea.getText();
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
	

	
	}


