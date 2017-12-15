package railview.simulation.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import py4j.GatewayServer;
import railapp.simulation.entries.TimetableSimulationEntry;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class EditorPaneController {
	@FXML
	private AnchorPane codePane;
	
	@FXML
	private TextArea infoArea;
	
	@FXML
	private Button playButton;
	
	@FXML
	private Button saveButton;
	
	private CodeArea codeArea = new CodeArea();
	
	private FileChooser fileChooser = new FileChooser();
	private File file;
	
	private static final String[] KEYWORDS = new String[] {
        "abstract", "assert", "boolean", "break", "byte",
        "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else",
        "enum", "extends", "final", "finally", "float",
        "for", "goto", "if", "implements", "import",
        "instanceof", "int", "interface", "long", "native",
        "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super",
        "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "void", "volatile", "while"
         
        /** Python Keywords
	       	"False", "class", "finally", "is", "return", "None",
	        "continue", "for","lambda","try", "True", "def", "from",
	        "nonlocal", "while", "and", "del", "global", "not", "with",
	        "as", "elif", "if", "or", "yield", "assert", "else",
	        "import", "pass", "break", "except", "in", "raise"	 
	 	**/
	};

	private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
	private static final String PAREN_PATTERN = "\\(|\\)";
	private static final String BRACE_PATTERN = "\\{|\\}";
	private static final String BRACKET_PATTERN = "\\[|\\]";
	private static final String SEMICOLON_PATTERN = "\\;";
	private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
	private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

	private static final Pattern PATTERN = Pattern.compile("(?<KEYWORD>"
			+ KEYWORD_PATTERN + ")" + "|(?<PAREN>" + PAREN_PATTERN + ")"
			+ "|(?<BRACE>" + BRACE_PATTERN + ")" + "|(?<BRACKET>"
			+ BRACKET_PATTERN + ")" + "|(?<SEMICOLON>" + SEMICOLON_PATTERN
			+ ")" + "|(?<STRING>" + STRING_PATTERN + ")" + "|(?<COMMENT>"
			+ COMMENT_PATTERN + ")");

 
	private static StyleSpans<Collection<String>> computeHighlighting(
			String text) {
		Matcher matcher = PATTERN.matcher(text);
		int lastKwEnd = 0;
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		while (matcher.find()) {
			String styleClass = matcher.group("KEYWORD") != null ? "keyword"
					: matcher.group("PAREN") != null ? "paren" : matcher
							.group("BRACE") != null ? "brace" : matcher
							.group("BRACKET") != null ? "bracket" : matcher
							.group("SEMICOLON") != null ? "semicolon" : matcher
							.group("STRING") != null ? "string" : matcher
							.group("COMMENT") != null ? "comment" : null; /*
																		 * never
																		 * happens
																		 */
			assert styleClass != null;
			spansBuilder.add(Collections.emptyList(), matcher.start()
					- lastKwEnd);
			spansBuilder.add(Collections.singleton(styleClass), matcher.end()
					- matcher.start());
			lastKwEnd = matcher.end();
		}
		spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
		return spansBuilder.create();
	}
 
	@FXML
	public void initialize() {
		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

		codeArea.richChanges()
				.filter(ch -> !ch.getInserted().equals(ch.getRemoved())).subscribe(
					change -> {
						codeArea.setStyleSpans(0,
							computeHighlighting(codeArea.getText()));
					});
		codeArea.prefWidthProperty().bind(codePane.widthProperty());
		codeArea.prefHeightProperty().bind(codePane.heightProperty());
		this.codePane.getChildren().add(codeArea);

		codeArea.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				playButton.setDisable(true);
				saveButton.setDisable(false);
			}
		});
		// codePane.getStylesheets().add(SimulationViewResources.class.getResource("/CSS/syntax-highlight.css").toExternalForm());
	}
 	
	
	@FXML
	protected void onNew(ActionEvent event) {
		this.codeArea.clear();
		this.file = null;
	}

	@FXML
	protected void onLoad(ActionEvent event) {
		this.file = fileChooser.showOpenDialog(null);
		if(this.file != null) {
			this.codeArea.clear();
			BufferedReader bufferedReader = null;
			try {
				String currentLine;
				bufferedReader = new BufferedReader(new FileReader(this.file));
				while((currentLine = bufferedReader.readLine()) != null)
					codeArea.appendText(currentLine + "\n");
					saveButton.setDisable(true);
					playButton.setDisable(false);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@FXML
	protected void onSave(ActionEvent event) {
		String content = this.codeArea.getText();
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
				saveButton.setDisable(true);
				playButton.setDisable(false);
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
					saveButton.setDisable(true);
					playButton.setDisable(false);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@FXML
	private void onSaveAs() {
		this.file = this.fileChooser.showSaveDialog(null);
		
		String content = this.codeArea.getText();
		if(this.file != null) {
			Stage stage = (Stage) this.codeArea.getScene().getWindow();
			stage.setTitle(this.file.getName() + " - jNotepad");
			try {
				if(!this.file.exists())
					this.file.createNewFile();
				
				FileWriter fileWriter = new FileWriter(this.file.getAbsoluteFile());
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(content);
				bufferedWriter.close();
				saveButton.setDisable(true);
				playButton.setDisable(false);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@FXML
	protected void onPlay(ActionEvent event) {
		this.infoArea.clear();
		
		try {        
			Thread thread = new Thread(() -> {
				try {
					GatewayServer gatewayServer = new GatewayServer(new TimetableSimulationEntry());
					
					gatewayServer.start();
					
			        ProcessBuilder pb = new ProcessBuilder("python", file.getPath());
			        
			        this.infoArea.appendText("Start and run Python script ... \n");
			        					
			        Process p = pb.start();

				    StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());
					StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());
					
					outputGobbler.start();
	                errorGobbler.start();
	                
	                p.waitFor();
	                
	                gatewayServer.shutdown();
				} catch (Exception e) {}
			});
			
			thread.start();
		} catch (Exception e) { }
	}
	
	private class StreamGobbler extends Thread {
	    InputStream is;

	    private StreamGobbler(InputStream is) {
	        this.is = is;
	    }

	    @Override
	    public void run() {
	        try {
	            InputStreamReader isr = new InputStreamReader(is);
	            BufferedReader br = new BufferedReader(isr);
	            String line = null;
	            while ((line = br.readLine()) != null) {
	                String str =line;
	                Platform.runLater(new Runnable() {
	                    @Override
	                    public void run() {
	                        infoArea.appendText(">" + str + "\n");
	                    }
	                });
	            }
	        } catch (IOException ioe) {
	            ioe.printStackTrace();
	        }
	    }
	}
}


