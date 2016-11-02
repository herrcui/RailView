import gnu.io.*;

import java.util.Enumeration;

public class DummyMsgGenerator {

	public static void main(String[] args) {
		CommPortIdentifier serialPortId;
		Enumeration<?> enumComm;
		
		enumComm = CommPortIdentifier.getPortIdentifiers();
		while (enumComm.hasMoreElements()) {
			serialPortId = (CommPortIdentifier) enumComm.nextElement();
			if (serialPortId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				System.out.println(serialPortId.getName());
			}
		}
	}
}
