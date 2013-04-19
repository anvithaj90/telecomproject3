package services;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import datatypes.Datagram;

public class TTPService {
	public TTPService(){

	}
	String payload= "Hello World!";
	int seq_num = 0;
	int ack = 0;
	char flag = 'S';
	
	private static DatagramService ds;
	
	public void connection_open(int port) throws SocketException {
		ds = new DatagramService(port, 10);
	}
	public void send_data(String dest_port, String src_port, String src_ip, String dest_ip) throws IOException	{
		if(ds==null){
			ds = new DatagramService(Integer.parseInt(src_port), 10);
		}
		byte[] datas;
		//int i = 0;
		int j = 0;
		byte[] header = new byte[6];
		byte[] payload = new byte[50];
		byte[] data = new byte[header.length + payload.length];
		String data1 = "Hello World!";
		for(int i=0;i<data1.length();i+=2){
			if(data1.length()>2){
				header = create_header(seq_num,ack,flag);
				payload = create_payload(data1.substring(i, i+2));
			}
		System.arraycopy(header, 0, data, 0, header.length);
		System.arraycopy(payload, 0, data, header.length, payload.length);
		//String datastring = new Arrays.toString(data);		
		Datagram datagram = new Datagram();
		datagram.setData(data);
		datagram.setSrcaddr(src_ip);
		datagram.setDstaddr(dest_ip);
		datagram.setDstport(Short.parseShort(dest_port));
		datagram.setSrcport(Short.parseShort(src_port));
		System.out.println(datagram.toString());
		ds.sendDatagram(datagram);
		}
	}
	public byte[] receive_data() throws ClassNotFoundException, IOException {
		Datagram datagram = ds.receiveDatagram();
		String temp = null;
		temp = (String) datagram.getData();
		byte[] data = temp.getBytes();	
		byte[] data1 = new byte[50];
		int i = 0;
		while(data[5] != 0x02) {
			data1[i] = data[6];
			data[i+1] = data[7];
			i += 2;
		}
		data1[i] = data[6];
		data[i+1] = data[7];
		System.out.println(data1.toString());
		return data1;
	}
	public void connection_close() { 
		
	}
	
	public byte[] create_header(int seq_num, int ack, char flag) {
		byte[] header = new byte[6];
		header[0] = (byte)(seq_num & 0xFF);
		header[1] = (byte)((seq_num >> 8) & 0xFF);
		header[3] = (byte)(ack & 0xFF);
		header[4] = (byte)((ack >> 8) & 0xFF);
	//S == SYN
		if(flag == 'S')
			header[5] = 0x01;
		// F == FIN end of file
		else if(flag == 'F')
			header[5] = 0x02;
		//C == close connection
		else if(flag == 'C')
			header[5] = 0x04;
		return header;
		
	}
	
	public byte[] create_payload(String payload) {
		byte[] payload_byte = payload.getBytes(); 	    	
		return payload_byte;
	}
	
}
