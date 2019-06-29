package ftp17;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class Ftp17Packet {

	public static final int FTP17_PORT = 9000;

	// Op Codes
	public static final short UPLOAD	= 1;
	public static final short ERROR 	= 2;
	public static final short DATA 		= 3;
	public static final short ACK 		= 4;
	public static final short FIN 		= 6;
	public static final short FINACK	= 7;


	public static final int MAX_FTP17_PACKET_SIZE = (1<<15);
	public static final int FTP17_SCRATCHPAD_SIZE = 16;
	private static final int FTP17_FIXED_HEADER = Short.BYTES + Long.BYTES + FTP17_SCRATCHPAD_SIZE;

	public static final byte[] DUMMY_SCRATCHPAD = new byte[FTP17_SCRATCHPAD_SIZE];

	protected byte[] packet;
	protected ByteBuffer bb;

	/**
	 * Constructor for creating a new, initially, empty Ftp17Packet
	 * 
	 **/
	public Ftp17Packet() {
		bb = ByteBuffer.allocate(MAX_FTP17_PACKET_SIZE);
		packet = bb.array();
	}

	/**
	 * Constructor for decoding a byte array as a Ftp17Packet
	 * 
	 **/
	public Ftp17Packet(byte[] packet, int length) {
		this.packet = packet;
		this.bb = ByteBuffer.wrap(packet, 0, length);
		this.bb.position(length); // ensure internal bb position is at "length"
	}

			
	public DatagramPacket toDatagram(SocketAddress dst) throws IOException {
		return new DatagramPacket(this.getPacketData(), this.getLength(), dst);
	}

	/**
	 * Gets the opcode from the first two bytes of the packet, stored in net
	 * byte order (Big Endian)
	 */
	public int getOpcode() {
		return bb.getShort(0);
	}

	/**
	 * 
	 * @return the size of the Ftp17Packet in bytes
	 */
	protected int getLength() {
		return bb.position();
	}

	/**
	 * 
	 * @return the byte array containing the Ftp17Packet
	 */
	protected byte[] getPacketData() {
		byte[] res = new byte[getLength()];
		System.arraycopy(packet, 0, res, 0, res.length);
		return res;
	}

	/**
	 * Assuming the Ftp17Packet is an UPLOAD
	 * 
	 * @return the filename
	 */
	public String getFilename() {
		return new String(packet, FTP17_FIXED_HEADER, getLength() - FTP17_FIXED_HEADER);
	}


	/**
	 * Assuming the Ftp17Packet is an ERROR
	 * 
	 * @return the error messagem
	 */
	public String getError() {
		return new String(packet, FTP17_FIXED_HEADER, getLength() - FTP17_FIXED_HEADER);
	}

	/**
	 * Return the sequence number of the Ftp17Packet
	 * 
	 * @return the sequence number
	 */
	public long getSeqN() {
		return bb.getLong(2);
	}

	/**
	 * Assuming the Ftp17Packet is a DATA
	 * 
	 * @return the byte array with the data payload
	 */
	public byte[] getBlockData() {
		final int offset = FTP17_FIXED_HEADER;
		byte[] res = new byte[getLength() - offset];
		System.arraycopy(packet, offset, res, 0, res.length);
		return res;
	}

	/**
	 * Get the byte array scratch pad area of the Ftp17Packet
	 * 
	 * @return the byte array with the data payload
	 */

	public byte[] getScratchPad() {
		final int offset = Short.BYTES + Long.BYTES;
		byte[] res = new byte[FTP17_SCRATCHPAD_SIZE];
		System.arraycopy(packet, offset, res, 0, res.length);
		return res;
	}

	/**
	 * Gets a byte from the scratch pad area of a Ftp17Packet
	 * 
	 */
	public int getSByte(int index) {
		return bb.get( index + FTP17_FIXED_HEADER - FTP17_SCRATCHPAD_SIZE);
	}

	/**
	 * Gets a short (2 bytes, in net order) from the scratch pad area of a Ftp17Packet
	 * 
	 */
	public short getSShort(int index) {
		return bb.getShort( index + FTP17_FIXED_HEADER - FTP17_SCRATCHPAD_SIZE);
	}

	/**
	 * Gets a long (8 bytes, in net order) from the scratch pad area of a Ftp17Packet
	 * 
	 */
	public long getSLong(int index) {
		return bb.getLong( index + FTP17_FIXED_HEADER - FTP17_SCRATCHPAD_SIZE);
	}

	/**
	 * Appends a byte to the Ftp17Packet
	 * 
	 */
	public Ftp17Packet putByte(int b) {
		bb.put((byte) b);
		return this;
	}

	/**
	 * Appends a short (2 bytes, in net order) to the Ftp17Packet
	 * 
	 */
	public Ftp17Packet putShort(int s) {
		bb.putShort((short) s);
		return this;
	}

	/**
	 * Appends a long (8 bytes, in net order) to the Ftp17Packet
	 * 
	 */
	public Ftp17Packet putLong(long l) {
		bb.putLong(l);
		return this;
	}

	/**
	 * Appends a string (ascii 8-bit chars) to the Ftp17Packet [does not include
	 * '\0' to terminate the string]
	 * 
	 */
	public Ftp17Packet putString(String s) {
		bb.put(s.getBytes());
		return this;
	}

	/**
	 * Appends the given (block) byte array to the Ftp17Packet
	 * 
	 */
	public Ftp17Packet putBytes(byte[] block) {
		return this.putBytes( block, block.length);
	}
	
	/**
	 * Appends length bytes of the given (block) byte array to the Ftp17Packet
	 * 
	 */
	public Ftp17Packet putBytes(byte[] block, int length) {
		bb.put(block, 0, length);
		return this;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		switch (this.getOpcode()) {
		case UPLOAD:
			sb.append("UPLOAD<").append( this.getFilename() );
			break;
		case ERROR:
			sb.append("ERROR<").append( this.getError() );
			break;
		case DATA:
			sb.append("DATA<").append( this.getSeqN()).append(" : ").append( this.getBlockData().length );
			break;
		case ACK:
			sb.append("ACK<").append(this.getSeqN() );
			break;
		case FIN:
			sb.append("FIN<").append(this.getSeqN() );
			break;
		case FINACK:
			sb.append("FINACK<").append(this.getSeqN() );
			break;
		}
		return sb.append('>').toString();
	}
}
