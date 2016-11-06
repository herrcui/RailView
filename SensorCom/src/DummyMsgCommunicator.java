import gnu.io.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;


public class DummyMsgCommunicator {
	static OutputStream out;
	static private FileWriter writer;
	int interval = 1000;

	void setWriter(CommPortIdentifier serialPortId) {
		CommPort commPort;
		SerialPort serialPort = null;

		try {
			commPort = serialPortId.open("DummySending", 2000);
			serialPort = (SerialPort) commPort;
			serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			out = serialPort.getOutputStream();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		new Timer().schedule(new TimerTask() {
			public void run() {
				String now = LocalDateTime.now().toString();
				try {
					out.write(now.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, interval, interval);
	}

	void setReader(String portName) throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			System.out.println("Error: Port is currently in use");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);

				InputStream in = serialPort.getInputStream();

				serialPort.addEventListener(new SerialReaderEvent(in));
                serialPort.notifyOnDataAvailable(true);

			} else {
				System.out.println("Error: Only serial ports are handled by this example.");
			}
		}
	}

	/**
	 * Handles the input coming from the serial port. A new line character is
	 * treated as the end of a block in this example.
	 */
	public static class SerialReaderEvent implements SerialPortEventListener {
		private InputStream in;
		private byte[] buffer = new byte[1024];

		public SerialReaderEvent(InputStream in) {
			this.in = in;
			
			try {
				File file;
				file = new File("c:\\temp\\logComm.csv");
				writer = new FileWriter(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void serialEvent(SerialPortEvent arg0) {
			int data;

			try {
				int len = 0;
				while ((data = in.read()) > -1) {
					if (data == '\n') {
						break;
					}
					buffer[len++] = (byte) data;
				}
				
				StringBuffer msgBuffer = new StringBuffer(new String(buffer, 0, len));
				msgBuffer.append("\n");
				System.out.println(msgBuffer);
				
				writer.write(msgBuffer.toString());
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public static void main(String[] args) {
		CommPortIdentifier serialPortId;
		Enumeration<?> enumComm;

		enumComm = CommPortIdentifier.getPortIdentifiers();
		while (enumComm.hasMoreElements()) {
			serialPortId = (CommPortIdentifier) enumComm.nextElement();
			if (serialPortId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				String portName = serialPortId.getName();
				System.out.println(portName);

				if (portName.equals("COM5")) {
					(new DummyMsgCommunicator()).setWriter(serialPortId);
				}

				if (portName.equals("COM6")) {
					try {
						(new DummyMsgCommunicator()).setReader("COM6");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
		
		new Timer().schedule(new TimerTask() {
			int i = 0;
			
			public void run() {
				if (i > 10) {
					try {
						writer.flush();
						writer.close();
						System.exit(-1);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					i++;
				}
			}
		}, 1000, 1000);
	}
}
