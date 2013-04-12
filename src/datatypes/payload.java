package datatypes;

public class payload {
	Integer sequence_no;
	Integer window_size;
	Integer ack;
	Integer timeout;
	
	public payload() {
		super();
	}
	public payload(Integer sequence_no, Integer window_size, Integer ack, Integer timeout) {
		super();
		this.sequence_no = sequence_no;
		this.window_size = window_size;
		this.ack = ack;
		this.timeout = timeout;
		
	}
	public Integer getSequence_no() {
		return sequence_no;
	}
	public void setSequence_no(Integer sequence_no) {
		this.sequence_no = sequence_no;
	}
	public Integer getWindow_size() {
		return window_size;
	}
	public void setWindow_size(Integer window_size) {
		this.window_size = window_size;
	}
	public Integer getAck() {
		return ack;
	}
	public void setAck(Integer ack) {
		this.ack = ack;
	}
	public Integer getTimeout() {
		return timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

}
