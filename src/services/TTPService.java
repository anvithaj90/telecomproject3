package services;

import java.io.IOException;
import java.net.SocketException;
import datatypes.Datagram;

public class TTPService {
	String payload= null;
	Integer seq_num;
	Integer ack;
	short window_size;
	char flag;
	
	private static DatagramService ds;
	
	public void connection_open(int port) throws SocketException {
		ds = new DatagramService(port, 10);
	}
	public void send_data(String dest_port, String src_port) throws IOException	{
		
		String[] datas = null;
		int i = 0;
		int j = 0;
		String data = "Hello World!";
		while(!data.equals(null)) {
			if(data.length()>2) {
				datas[i] = data.substring(j, j+2);
				i++;
			}
		}
					
		payload = data + seq_num.toString() + ack.toString() + (char)window_size + flag;
		Datagram datagram = new Datagram();
		datagram.setData(payload);
		datagram.setSrcaddr("127.0.0.1");
		datagram.setDstaddr("127.0.0.1");
		datagram.setDstport((short)Integer.parseInt(dest_port));
		datagram.setSrcport((short)Integer.parseInt(src_port));
		ds.sendDatagram(datagram);
	}
	public void receive_data() throws ClassNotFoundException, IOException {
		ds.receiveDatagram();
	}
	public void connection_close() {
		
	}
	
	
	
}
