package railview.simulation.pyui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import py4j.GatewayServer;
import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.Link;
import railapp.infrastructure.element.geometry.dto.IGeometry;
import railapp.infrastructure.element.geometry.dto.SimpleGeometry;
import railapp.infrastructure.path.dto.LinkEdge;
import railapp.infrastructure.path.dto.LinkPath;
import railapp.simulation.SingleSimulationManager;
import railapp.simulation.calibration.deterministic.Calibrator;
import railapp.simulation.entries.Py4JGateway;
import railapp.simulation.infrastructure.ResourceOccupancy;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.timetable.dto.Trip;
import railapp.units.Length;
import railapp.units.Velocity;
import railview.simulation.SimulationFactory;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

/**
 * The controller class of EditorPane.fxml. In the Pane, you can write and load
 * code on the left side and the outcome is on the right side.
 *
 */
public class PythonPaneController {
	private GatewayServer gatewayServer = null;

	@FXML
	private TextField textIndex, textVStart, textVEnd, textVStep, textLStart, textLEnd, textLStep, textOutputFile;

	@FXML
	private TextField textPath, textRound, textA, textB, textC, textAcc, textBrake;

	@FXML
	private Button calculateButton, calibrateButton;

	@FXML
	private TableView<HeadwayInfo> tableViewResult;

	@FXML
	private Label labelResult;

	@FXML
	private TextArea textLog;

	private SimulationFactory simulationFactory;

	@FXML
	private AnchorPane pythonPane, fixedButtonPane, codePane;

	@FXML
	private Button playButton, saveButton;

	@FXML
	private Button pyActiveButton, pyDeactiveButton;

	@FXML
	private Image pyImgBW;

	@FXML
	private TextArea infoArea;

	private Calibrator calibrator;

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
	@FXML
	public void initialize() {
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
				playButton.setDisable(true);
				saveButton.setDisable(false);
			}
		});

		SplitPane.setResizableWithParent(fixedButtonPane, Boolean.FALSE);

		pyDeactiveButton.setVisible(false);

		// set tableview
		TableColumn<HeadwayInfo, Double> velocityColumn = new TableColumn<HeadwayInfo, Double>("Velocity");
        velocityColumn.setCellValueFactory(new PropertyValueFactory<>("velocity"));

        TableColumn<HeadwayInfo, Double> meterColumn = new TableColumn<HeadwayInfo, Double>("Meter");
        meterColumn.setCellValueFactory(new PropertyValueFactory<>("meter"));

        TableColumn<HeadwayInfo, Double> headwayColumn = new TableColumn<HeadwayInfo, Double>("Headway");
        headwayColumn.setCellValueFactory(new PropertyValueFactory<>("headway"));

        this.tableViewResult.getColumns().addAll(velocityColumn, meterColumn, headwayColumn);
	}

	public void setSimulationFactory(SimulationFactory factory) {
		this.simulationFactory = factory;
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
	private void onSaveAs() {
		this.file = this.fileChooser.showSaveDialog(null);

		String content = this.codeArea.getText();
		if (this.file != null) {
			Stage stage = (Stage) this.codeArea.getScene().getWindow();
			stage.setTitle(this.file.getName() + " - jNotepad");
			try {
				if (!this.file.exists())
					this.file.createNewFile();

				FileWriter fileWriter = new FileWriter(
						this.file.getAbsoluteFile());
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(content);
				bufferedWriter.close();
				saveButton.setDisable(true);
				playButton.setDisable(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@FXML
	protected void onNew(ActionEvent event) {
		this.codeArea.clear();
		this.file = null;
	}

	@FXML
	protected void onLoad(ActionEvent event) {
		this.file = fileChooser.showOpenDialog(null);
		if (this.file != null) {
			this.codeArea.clear();
			BufferedReader bufferedReader = null;
			try {
				String currentLine;
				bufferedReader = new BufferedReader(new FileReader(this.file));
				while ((currentLine = bufferedReader.readLine()) != null)
					codeArea.appendText(currentLine + "\n");
				saveButton.setDisable(true);
				playButton.setDisable(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@FXML
	protected void onSave(ActionEvent event) {
		String content = this.codeArea.getText();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
				"TXT files (*.txt)", "*.txt");
		fileChooser.getExtensionFilters().add(extFilter);
		if (this.file != null) {
			try {
				if (!this.file.exists())
					this.file.createNewFile();
				FileWriter fileWriter = new FileWriter(
						this.file.getAbsoluteFile());
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(content);
				bufferedWriter.close();
				saveButton.setDisable(true);
				playButton.setDisable(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {

			this.file = this.fileChooser.showSaveDialog(null);
			if (this.file != null) {
				try {
					if (!this.file.exists())
						this.file.createNewFile();
					FileWriter fileWriter = new FileWriter(
							this.file.getAbsoluteFile());
					BufferedWriter bufferedWriter = new BufferedWriter(
							fileWriter);
					bufferedWriter.write(content);
					bufferedWriter.close();
					saveButton.setDisable(true);
					playButton.setDisable(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@FXML
	protected void onPyAction(ActionEvent event) {
		try {
			if (this.pyActiveButton.visibleProperty().getValue()) {
				//gatewayServer = new GatewayServer(
				//	new TimetableSimulationEntry());
				gatewayServer = new GatewayServer(new Py4JGateway());

				this.pyActiveButton.setVisible(false);
				this.pyDeactiveButton.setVisible(true);

				Stage stage = (Stage) this.pythonPane.getScene().getWindow();
				stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				    public void handle(WindowEvent we) {
				    	System.out.println(gatewayServer);
				        if (gatewayServer != null) {
				        	System.out.println("closing gatewayserver");
				        	gatewayServer.shutdown();
				        }
				    }
				});

				gatewayServer.start();

			} else {
				gatewayServer.shutdown();
				gatewayServer = null;
				this.pyActiveButton.setVisible(true);
				this.pyDeactiveButton.setVisible(false);
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@FXML
	protected void onPlay(ActionEvent event) {
		this.infoArea.clear();
		try {
			Thread thread = new Thread(() -> {
				try {
					ProcessBuilder pb = new ProcessBuilder("python",
							file.getPath());

					this.infoArea
							.appendText("Start and run Python script ... \n");

					Process p = pb.start();

					StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());
					StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());

					outputGobbler.start();
					errorGobbler.start();

					p.waitFor();
				} catch (Exception e) {
					System.out.println(e);
				}
			});

			thread.start();
		} catch (Exception e) {
		}
	}

	@FXML
	protected void calculateHeadway() {
		if (this.simulationFactory == null) {
			return;
		}

		//this.tableViewResult.getItems().clear();

		double minHeadWay = Double.MAX_VALUE;
        double minHeadwayAtSpeed = -1;
        double minHeadwayAtMeter = -1;

        FileWriter writer = null;

        try {
	        File file = new File(this.textOutputFile.getText());
	        file.createNewFile();
			writer = new FileWriter(file);
			writer.write("meter;velocity;headway\n");
        } catch (Exception e) {}

		int indexOfTrip = Integer.parseInt(this.textIndex.getText()) - 1;

		HashMap<Link, List<IGeometry>> backup = this.backUpGeometries(indexOfTrip);

        for (double speed = Integer.parseInt(this.textVStart.getText()); speed <= Integer.parseInt(this.textVEnd.getText());
        			speed = speed+Integer.parseInt(this.textVStep.getText())) {
        	for (double meter = Integer.parseInt(this.textLStart.getText()); meter <= Integer.parseInt(this.textLEnd.getText());
        			meter = meter+Integer.parseInt(this.textLStep.getText())) {
        		this.setGeometries(indexOfTrip, meter, speed);

        		SingleSimulationManager simulator = SingleSimulationManager.getInstance(
                        this.simulationFactory.getInfraServiceUtility(),
                        this.simulationFactory.getRollingStockServiceUtility(),
                        this.simulationFactory.getTimeTableServiceUtility());

                simulator.run();

                AbstractTrainSimulator train = simulator.getTrainSimulators().get(0);
                List<ResourceOccupancy> resourceOccupancies = train.getBlockingTimeStairWay();
                double headway = 0;

                for (int i = 1; i < resourceOccupancies.size() - 1; i++) {
                	ResourceOccupancy occupancy = resourceOccupancies.get(i);
                	if (occupancy.getDuration().getTotalSeconds() > headway) {
                		headway = occupancy.getDuration().getTotalSeconds();
                	}
                }

                if (headway < minHeadWay) {
                	minHeadWay = headway;
                	minHeadwayAtSpeed = speed;
                	minHeadwayAtMeter = meter;
                }

                this.tableViewResult.getItems().add(new HeadwayInfo(speed, meter, headway));

                String line = meter+";"+speed+";"+headway+";\n";

                try {
                	writer.write(line);
                } catch (Exception e) {
                	System.out.println(e);
                }

                for (Link link : backup.keySet()) {
                	link.setGeometries(backup.get(link));
                }
        	}
        }

        this.labelResult.setText("Result: At Meter: " + minHeadwayAtMeter + " with Speed limit: " + minHeadwayAtSpeed + " min. Headway (sec): " + minHeadWay);

	    try {
	        writer.flush();
	        writer.close();
        } catch (Exception e) {}
	}

	private HashMap<Link, List<IGeometry>> backUpGeometries(int indexOfTrip) {
		HashMap<Link, List<IGeometry>> backup = new HashMap<Link, List<IGeometry>>();
		for (Trip trip : this.simulationFactory.getTimeTableServiceUtility().getTimetableService().findAllTrips()) {
			LinkPath path = trip.getTripSections().get(0).getNextPathStartFrom(indexOfTrip);
			for (LinkEdge edge : path.getEdges()) {
				backup.put(edge.getLink(), edge.getLink().getGeometries());
			}
		}
		return backup;
	}

	private void setGeometries(int indexOfTrip, double meter, double speed) {
		double totalMeter = 0;

		for (Trip trip : this.simulationFactory.getTimeTableServiceUtility().getTimetableService().findAllTrips()) {
			LinkPath path = trip.getTripSections().get(0).getNextPathStartFrom(indexOfTrip);
			Link link = null;
			List<IGeometry> geometries = null;
			for (int i=path.getEdges().size()-1; i>=0; i--) {
				LinkEdge edge = path.getEdges().get(i);

				if (link == null) {
					link = edge.getLink();
					geometries = new ArrayList<IGeometry>();
				} else {
					if (link != edge.getLink()) {
						link.setGeometries(geometries);
						link = edge.getLink();
						geometries = new ArrayList<IGeometry>();
					}
				}

				List<LinkEdge> splitEdges = edge.splitByGeometry();

				// reach to the length
				if (totalMeter + edge.getLength().getMeter() >= meter) {
					double restMeter = meter - totalMeter;
					boolean isReached = false;

					for (int j=splitEdges.size()-1; j>=0; j--) {
						LinkEdge splitEdge = splitEdges.get(j);

						if (isReached) {
							geometries.add(0, new SimpleGeometry(splitEdge.getLength(), splitEdge.getMaxVelocityAtEnd()));
							continue;
						}

						if (splitEdge.getLength().getMeter() <= restMeter) {
							geometries.add(0, new SimpleGeometry(splitEdge.getLength(),
								Velocity.fromKilometerPerHour(Double.min(speed, splitEdge.getMaxVelocityAtEnd().getKilometerPerHour()))));
							restMeter = restMeter - splitEdge.getLength().getMeter();
							if (restMeter == 0) {
								isReached = true;
							}
						} else {
							geometries.add(0, new SimpleGeometry(Length.fromMeter(restMeter),
								Velocity.fromKilometerPerHour(Double.min(speed, splitEdge.getMaxVelocityAtEnd().getKilometerPerHour()))));
							geometries.add(0, new SimpleGeometry(Length.fromMeter(splitEdge.getLength().getMeter() - restMeter),
								Velocity.fromKilometerPerHour(splitEdge.getMaxVelocityAtEnd().getKilometerPerHour())));
							isReached = true;
						}
					}

					double processedMeter = 0;
					for (IGeometry geometry : geometries) {
						processedMeter += geometry.getLength().getMeter();
					}
					// put rest of the geometry of the link
					totalMeter = 0;
					isReached = false;
					for (IGeometry geometry : link.getGeometries()) {
						if (isReached) {
							geometries.add(geometry);
						} else {
							if (totalMeter + geometry.getLength().getMeter() > processedMeter) {
								geometries.add(new SimpleGeometry(Length.fromMeter(totalMeter + geometry.getLength().getMeter() - processedMeter),
										geometry.getMaxVelocity()));
							}
						}
						totalMeter += geometry.getLength().getMeter();
					}
					link.setGeometries(geometries);
					return;
				}

				for (int j=splitEdges.size()-1; j>=0; j--) {
					LinkEdge splitEdge = splitEdges.get(j);
					geometries.add(0, new SimpleGeometry(splitEdge.getLength(), Velocity.fromKilometerPerHour(
							Double.min(speed, splitEdge.getMaxVelocityAtEnd().getKilometerPerHour()))));

				}

				totalMeter += edge.getLength().getMeter();
			} // for (int i=path.getEdges().size()-1; i>=0; i--)
			link.setGeometries(geometries);
			return;
		}
	}

	public class HeadwayInfo {
		private double velocity;
		private double meter;
		private double headway;

		public HeadwayInfo(double velocity, double meter, double headway) {
			this.meter = meter;
			this.velocity = velocity;
			this.headway = headway;
		}

		public double getMeter() {
			return meter;
		}

		public double getVelocity() {
			return velocity;
		}

		public double getHeadway() {
			return headway;
		}
	}

	@FXML
	private void onCalibrate() {
		this.textLog.clear();

		List<Double> parameters = new ArrayList<Double>();
        parameters.add(this.parseParameter(textA));
        parameters.add(this.parseParameter(textB));
        parameters.add(this.parseParameter(textC));
        parameters.add(this.parseParameter(textAcc));
        parameters.add(this.parseParameter(textBrake));

        this.calibrator = Calibrator.getInstance(
        		this.simulationFactory.getInfraServiceUtility(),
        		this.simulationFactory.getRollingStockServiceUtility(),
        		this.simulationFactory.getTimeTableServiceUtility(),
        		parameters,
        		this.textPath.getText(),
        		this.textLog,
        		Integer.parseInt(this.textRound.getText()));
        //this.calibrator.start();

        this.calibrator.run();
	}

	private double parseParameter(TextField text) {
		try {
			return Double.parseDouble(text.getText());
		} catch (Exception e) {
			return Double.MAX_VALUE;
		}
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
					String str = line;
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

	private CodeArea codeArea = new CodeArea();
	private FileChooser fileChooser = new FileChooser();
	private File file;

}
