package railview.simulation.setting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import railapp.dispatching.DispatchingSystem;
import railapp.dispatching.NoneDispatchingSystem;
import railapp.dispatching.services.Py4JDispatchingSystem;
import railapp.messages.Message;
import railapp.simulation.SingleSimulationManager;
import railview.simulation.ui.data.TableProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
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
	private TableView<TableProperty> ILTable;

	@FXML
	private Button externalFileButton, applyButton, setCommButton;

	@FXML
	private TextField fileNameText, urlText, portText;

	@FXML
	private TableView<TableProperty> ReceivedTable, SentTable;

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

	@SuppressWarnings("unchecked")
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

		TableColumn<TableProperty, String> trainItemCol = new TableColumn<TableProperty, String>("Item");
		trainItemCol.setMinWidth(100);
		trainItemCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("item"));

		TableColumn<TableProperty, String> trainValueCol = new TableColumn<TableProperty, String>("Value");
		trainValueCol.setMinWidth(100);
		trainValueCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("value"));

		ILTable.getColumns().addAll(trainItemCol, trainValueCol);

		ILTable.setItems(generateILInfo());

		TableColumn<TableProperty, String> RMsgTimeCol = new TableColumn<TableProperty, String>("Time");
		RMsgTimeCol.setMinWidth(100);
		RMsgTimeCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("item"));

		TableColumn<TableProperty, String> RMsgContentCol = new TableColumn<TableProperty, String>("Content");
		RMsgContentCol.setMinWidth(100);
		RMsgContentCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("value"));

		ReceivedTable.getColumns().addAll(RMsgTimeCol, RMsgContentCol);

		TableColumn<TableProperty, String> SMsgTimeCol = new TableColumn<TableProperty, String>("Time");
		SMsgTimeCol.setMinWidth(100);
		SMsgTimeCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("item"));

		TableColumn<TableProperty, String> SMsgContentCol = new TableColumn<TableProperty, String>("Content");
		SMsgContentCol.setMinWidth(100);
		SMsgContentCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("value"));

		SentTable.getColumns().addAll(SMsgTimeCol, SMsgContentCol);
	}

	private static ObservableList<TableProperty> generateILInfo() {
		ObservableList<TableProperty> ILInfoList = FXCollections.observableArrayList();

		ILInfoList.add(new TableProperty("Release Time (s)", "3.0"));
		ILInfoList.add(new TableProperty("Route Setting Time with Junction (s)", "13.0"));
		ILInfoList.add(new TableProperty("Route Setting Time without Junction (s)", "8.0"));
		ILInfoList.add(new TableProperty("Transfer time from IL to RBC (s)", "0.5"));
		ILInfoList.add(new TableProperty("RBC Process Time (s)", "0.5"));
		ILInfoList.add(new TableProperty("GSMR Transfer Time (s)", "8.0"));
		ILInfoList.add(new TableProperty("GSMR_SCI Time (s)", "1.0"));
		ILInfoList.add(new TableProperty("On_Board Unit and Reaction Time (s)", "1.5"));

		return ILInfoList;
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
		this.fileNameText.setDisable(this.defaultRB.isSelected());
		this.codeArea.setDisable(this.defaultRB.isSelected());

		this.applyButton.setDisable(false);
	}

	@FXML
	private void onExternalRB(ActionEvent event) {
		this.defaultRB.setSelected(!this.externalRB.isSelected());
		this.externalFileButton.setDisable(this.defaultRB.isSelected());
		this.fileNameText.setDisable(this.defaultRB.isSelected());
		this.codeArea.setDisable(this.defaultRB.isSelected());

		if (this.externalRB.isSelected()) {
			this.fileNameText.setText("Select an external dispatching script");
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
			this.fileNameText.setText(file.getPath());
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

	@FXML
	private void onSetCommButton(ActionEvent event) {
		this.simulator.getDispatchingSystem().getDispCommunication().setRadioLibConn(
				this.urlText.getText(), Integer.parseInt(this.portText.getText()));
		System.out.println("socket has been set.");
	}

	public void updateMessages(List<Message>received, List<Message>sent) {
		if (received != null && received.size()>0) {
			this.addMessages(received, this.ReceivedTable);
		}

		if (sent != null && sent.size()>0) {
			this.addMessages(sent, this.SentTable);
		}
	}

	private void addMessages(List<Message> messages, TableView<TableProperty> tv) {
		CopyOnWriteArrayList<Message> tempList = new CopyOnWriteArrayList<Message>();
		tempList.addAll(messages);
		ObservableList<TableProperty> messageList = FXCollections.observableArrayList();

		for (Message message : tempList) {
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
			String timeText = timeFormat.format(new Date(message.getTxTimestamp()));
			messageList.add(new TableProperty(timeText,	message.toString()));
		}

		tv.setItems(messageList);
	}

	private void setDispatchingSystem() {
		if (this.simulator != null) {
			if (this.defaultRB.isSelected() &&
					!(this.simulator.getDispatchingSystem() instanceof NoneDispatchingSystem)) {
				this.simulator.setDispatchingSystem(NoneDispatchingSystem.getInstance());
			} else {
				if (this.file != null) {
					DispatchingSystem dispatcher = Py4JDispatchingSystem.getDefaultInstance(file.getPath());
					this.simulator.setDispatchingSystem(dispatcher);
				}
			}
		}
	}

	private FileChooser fileChooser = new FileChooser();
	private File file = null;

	private SingleSimulationManager simulator;
}
