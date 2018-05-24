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

/**
 * The controller class for Diaglog.fxml. Its used for loading the
 * Infrastructure, Rollingstock and the Timetable from a directory.
 * 
 */
public class DialogPaneController extends Stage implements Initializable {
	@FXML
	private AnchorPane anchorPane;

	@FXML
	private Button okButton, cancelButton, applyButton, rootButton,
			infraButton, rollingstockButton, timetableButton;

	@FXML
	private TextField textOne, textTwo, textThree;

	private Path infraPath, rollingStockPath, timetablePath;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		openRoot();
		openInfrastructure();
		openRollingstock();
		openTimetable();
	}

	public DialogPaneController(Parent parent) {
		setTitle("Filepaths");

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"Dialog.fxml"));
		fxmlLoader.setController(this);

		try {
			setScene(new Scene((Parent) fxmlLoader.load()));
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	@FXML
	private void onCancelButtonAction(ActionEvent event) {
		close();
	}

	@FXML
	private void onApplyButtonAction(ActionEvent event) {
		close();
	}

	private void openRoot() {
		rootButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				File file = directoryChooser.showDialog(anchorPane.getScene()
						.getWindow());
				directoryChooser.setInitialDirectory(file);
				if (file != null) {
					String infraPath = file.getParent().toString()
							+ "\\"
							+ file.getName().substring(0,
									file.getName().lastIndexOf('-'));
					String rollingstockPath = file.getParent().toString()
							+ "\\global\\dat";

					openInfra(new File(infraPath));
					openRolling(new File(rollingstockPath));
					openTime(file);
				}
			}
		});
	}

	private void openInfrastructure() {
		infraButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				configureFileChooser(directoryChooser);
				File file = directoryChooser.showDialog(anchorPane.getScene()
						.getWindow());
				if (file != null) {
					openInfra(file);
				}
			}
		});
	}

	private void openRollingstock() {
		rollingstockButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				configureFileChooser(directoryChooser);
				File file = directoryChooser.showDialog(anchorPane.getScene()
						.getWindow());
				if (file != null) {
					openRolling(file);
				}
			}
		});
	}

	private void openTimetable() {
		timetableButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				configureFileChooser(directoryChooser);
				File file = directoryChooser.showDialog(anchorPane.getScene()
						.getWindow());
				if (file != null) {
					openTime(file);
				}
			}
		});
	}

	private static void configureFileChooser(DirectoryChooser directoryChooser) {
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

	DirectoryChooser directoryChooser = new DirectoryChooser();
}
