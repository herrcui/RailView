package railview.simulation.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import railapp.dispatching.DispatchingSystem;
import railapp.dispatching.NoneDispatchingSystem;
import railapp.dispatching.services.ExternalDispatchingSystem;
import railapp.simulation.SingleSimulationManager;
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
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * The controller class for the ConfigurationPane.fxml. You can either write or
 * load a python script in this UI.
 * 
 */
public class SettingPaneController extends Stage implements Initializable {
	@FXML
	private AnchorPane settingPaneRoot, codePane;

	@FXML
	private RadioButton defaultRB, externalRB;

	@FXML
	private Button externalFileButton, applyButton;

	@FXML
	private Label fileNameLabel;

	private CodeArea codeArea;

	private static final String[] KEYWORDS = new String[] { "abstract",
			"assert", "boolean", "break", "byte", "case", "catch", "char",
			"class", "const", "continue", "default", "do", "double", "else",
			"enum", "extends", "final", "finally", "float", "for", "goto",
			"if", "implements", "import", "instanceof", "int", "interface",
			"long", "native", "new", "package", "private", "protected",
			"public", "return", "short", "static", "strictfp", "super",
			"switch", "synchronized", "this", "throw", "throws", "transient",
			"try", "void", "volatile", "while"

	/**
	 * Python Keywords "False", "class", "finally", "is", "return", "None",
	 * "continue", "for","lambda","try", "True", "def", "from", "nonlocal",
	 * "while", "and", "del", "global", "not", "with", "as", "elif", "if", "or",
	 * "yield", "assert", "else", "import", "pass", "break", "except", "in",
	 * "raise"
	 **/
	};

	private static final String KEYWORD_PATTERN = "\\b("
			+ String.join("|", KEYWORDS) + ")\\b";
	private static final String PAREN_PATTERN = "\\(|\\)";
	private static final String BRACE_PATTERN = "\\{|\\}";
	private static final String BRACKET_PATTERN = "\\[|\\]";
	private static final String SEMICOLON_PATTERN = "\\;";
	private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
	private static final String COMMENT_PATTERN = "//[^\n]*" + "|"
			+ "/\\*(.|\\R)*?\\*/";

	private static final Pattern PATTERN = Pattern.compile("(?<KEYWORD>"
			+ KEYWORD_PATTERN + ")" + "|(?<PAREN>" + PAREN_PATTERN + ")"
			+ "|(?<BRACE>" + BRACE_PATTERN + ")" + "|(?<BRACKET>"
			+ BRACKET_PATTERN + ")" + "|(?<SEMICOLON>" + SEMICOLON_PATTERN
			+ ")" + "|(?<STRING>" + STRING_PATTERN + ")" + "|(?<COMMENT>"
			+ COMMENT_PATTERN + ")");

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		codeArea = new CodeArea();
		codeArea.setPrefHeight(codePane.getPrefHeight());
		codeArea.setPrefWidth(codePane.getPrefWidth());
		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
		codeArea.richChanges()
				.filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
				.subscribe(
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
				applyButton.setDisable(false);
			}
		});
	}

	
	public SettingPaneController() {
		setTitle("Configurations");
		
		fileChooser.setTitle("Choose external Python dispatching file ...");
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
				"Python files (*.py)", "*.py");
		fileChooser.getExtensionFilters().add(extFilter);
	}

	public void setSimulator(SingleSimulationManager simulator) {
		this.simulator = simulator;

		this.setDispatchingSystem();
	}

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
	private void onDefaultRB(ActionEvent event) {
		this.externalFileButton.setDisable(this.defaultRB.isSelected());
		this.externalRB.setSelected(!this.defaultRB.isSelected());
		this.fileNameLabel.setDisable(this.defaultRB.isSelected());
		this.codeArea.setDisable(this.defaultRB.isSelected());

		this.applyButton.setDisable(false);
	}

	@FXML
	private void onExternalRB(ActionEvent event) {
		this.defaultRB.setSelected(!this.externalRB.isSelected());
		this.externalFileButton.setDisable(this.defaultRB.isSelected());
		this.fileNameLabel.setDisable(this.defaultRB.isSelected());
		this.codeArea.setDisable(this.defaultRB.isSelected());

		if (this.externalRB.isSelected()) {
			this.fileNameLabel.setText("Select an external dispatching script");
			this.codeArea.clear();
			this.file = null;
		}
	}

	@FXML
	private void onApply(ActionEvent event) {
		String content = this.codeArea.getText();

		if (this.file != null) {
			try {
				if (!this.file.exists())
					this.file.createNewFile();
				FileWriter fileWriter = new FileWriter(
						this.file.getAbsoluteFile());
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(content);
				bufferedWriter.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		this.setDispatchingSystem();
		this.applyButton.setDisable(true);
	}

	@FXML
	private void onExternalFileButton(ActionEvent event) {
		this.file = fileChooser.showOpenDialog(settingPaneRoot.getScene()
				.getWindow());

		if (this.file != null) {
			this.fileNameLabel.setText(file.getPath());
			this.codeArea.clear();
			BufferedReader bufferedReader = null;
			try {
				String currentLine;
				bufferedReader = new BufferedReader(new FileReader(file));
				while ((currentLine = bufferedReader.readLine()) != null)
					codeArea.appendText(currentLine + "\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void setDispatchingSystem() {
		if (this.simulator != null) {
			if (this.defaultRB.isSelected()) {
				this.simulator.setDispatchingSystem(NoneDispatchingSystem
						.getInstance());
			} else {
				if (this.file != null) {
					String className = file.getName().substring(0,
							file.getName().lastIndexOf('.'));
					DispatchingSystem dispatcher = ExternalDispatchingSystem
							.getInstanceFromServiceClass(className,
									file.getPath(), this.simulator);
					this.simulator.setDispatchingSystem(dispatcher);
				}
			}
		}
	}

	private FileChooser fileChooser = new FileChooser();
	private File file = null;

	private SingleSimulationManager simulator;
}
