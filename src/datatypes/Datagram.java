/*
 * DO NOT MODIFY ANY CLASS MEMBERS !!!
 */

package datatypes;

import java.io.Serializable;


// TODO: Auto-generated Javadoc
// Format of datagram packet
/**
 * The Class Datagram.
 */
public class Datagram implements Serializable {
	
	// Source IP address
	/** The srcaddr. */
	String srcaddr;
	
	// Destination IP address
	/** The dstaddr. */
	String dstaddr;
	
	// Source port
	/** The srcport. */
	short srcport;
	
	// Destination port
	/** The dstport. */
	short dstport;
	
	// Actual length of data section
	/** The size. */
	short size;
	
	// Datagram checksum
	/** The checksum. */
	short checksum;
	
	// Actual data
	/** The data. */
	Object data;

	
	/**
	 * Instantiates a new datagram.
	 */
	public Datagram() {
		super();
	}
	
	/**
	 * Instantiates a new datagram.
	 *
	 * @param srcaddr the srcaddr
	 * @param dstaddr the dstaddr
	 * @param srcport the srcport
	 * @param dstport the dstport
	 * @param size the size
	 * @param checksum the checksum
	 * @param data the data
	 */
	public Datagram(String srcaddr, String dstaddr, short srcport,
			short dstport, short size, short checksum, Object data) {
		super();
		this.srcaddr = srcaddr;
		this.dstaddr = dstaddr;
		this.srcport = srcport;
		this.dstport = dstport;
		this.size = size;
		this.checksum = checksum;
		this.data = data;
	}

	/**
	 * Gets the srcaddr.
	 *
	 * @return the srcaddr
	 */
	public String getSrcaddr() {
		return srcaddr;
	}

	/**
	 * Sets the srcaddr.
	 *
	 * @param srcaddr the srcaddr to set
	 */
	public void setSrcaddr(String srcaddr) {
		this.srcaddr = srcaddr;
	}

	/**
	 * Gets the dstaddr.
	 *
	 * @return the dstaddr
	 */
	public String getDstaddr() {
		return dstaddr;
	}

	/**
	 * Sets the dstaddr.
	 *
	 * @param dstaddr the dstaddr to set
	 */
	public void setDstaddr(String dstaddr) {
		this.dstaddr = dstaddr;
	}

	/**
	 * Gets the srcport.
	 *
	 * @return the srcport
	 */
	public short getSrcport() {
		return srcport;
	}

	/**
	 * Sets the srcport.
	 *
	 * @param srcport the srcport to set
	 */
	public void setSrcport(short srcport) {
		this.srcport = srcport;
	}

	/**
	 * Gets the dstport.
	 *
	 * @return the dstport
	 */
	public short getDstport() {
		return dstport;
	}

	/**
	 * Sets the dstport.
	 *
	 * @param dstport the dstport to set
	 */
	public void setDstport(short dstport) {
		this.dstport = dstport;
	}

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	public short getSize() {
		return size;
	}

	/**
	 * Sets the size.
	 *
	 * @param size the size to set
	 */
	public void setSize(short size) {
		this.size = size;
	}

	/**
	 * Gets the checksum.
	 *
	 * @return the checksum
	 */
	public short getChecksum() {
		return checksum;
	}

	/**
	 * Sets the checksum.
	 *
	 * @param checksum the checksum to set
	 */
	public void setChecksum(short checksum) {
		this.checksum = checksum;
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * Sets the data.
	 *
	 * @param data the data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}
	
	
}
