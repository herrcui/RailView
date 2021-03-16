package railview.simulation.setting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Properties;

public class UIInfrastructureSetting {
	private double signalCycle = 0.5;
	private double signalWidth = 0.1;
	private double elementWidth = 0.2;
	private boolean isShowStation = true;

	private Path propertyPath = null;

	public static UIInfrastructureSetting getInstance(Path path) {
		UIInfrastructureSetting setting = new UIInfrastructureSetting();

		try {
			File file = new File(path.toString() + "\\setting.properties");
			if (file.exists()) {
				InputStream in = new FileInputStream(file);
				Properties settingProp = new Properties();
				settingProp.load(in);
				setting.setElementWidth(Double.parseDouble(settingProp.getProperty("UI.element_width")));

				setting.propertyPath = path;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return setting;
	}

	private UIInfrastructureSetting() {

	}

	public double getSignalCycle() {
		return signalCycle;
	}

	public void setSignalCycle(double signalCycle) {
		this.signalCycle = signalCycle;
	}

	public double getSignalWidth() {
		return signalWidth;
	}

	public void setSignalWidth(double signalWidth) {
		this.signalWidth = signalWidth;
	}

	public double getElementWidth() {
		return elementWidth;
	}

	public void setElementWidth(double elementWidth) {
		this.elementWidth = elementWidth;
	}

	public boolean isShowStation() {
		return isShowStation;
	}

	public void setShowStation(boolean isShowStation) {
		this.isShowStation = isShowStation;
	}

	public void save() {
		try (final OutputStream outputstream
                = new FileOutputStream(this.propertyPath.toString() + "\\setting.properties");) {
			Properties settingProp = new Properties();
			settingProp.setProperty("UI.element_width", String.valueOf(this.getElementWidth()));

			settingProp.store(outputstream,"File Updated");
			outputstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
