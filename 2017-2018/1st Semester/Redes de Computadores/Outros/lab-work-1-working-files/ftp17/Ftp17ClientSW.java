package ftp17;

import static ftp17.Ftp17Packet.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


public class Ftp17ClientSW {
	static final int DEFAULT_TIMEOUT = 1000;
	static final int DEFAULT_MAX_RETRIES = 5;
	private static final int DEFAULT_BLOCK_SIZE = 1024;
	static int WindowSize = DEFAULT_BLOCK_SIZE; // this client is a stop and wait one
	static int BlockSize = DEFAULT_BLOCK_SIZE;
	static int Timeout = DEFAULT_TIMEOUT;

	private Stats stats;
	private String filename;
	private DatagramSocket socket;
	private BlockingQueue<Ftp17Packet> receiverQueue;
	volatile private SocketAddress srvAddress;

	Ftp17ClientSW(String filename, SocketAddress srvAddress) {
		this.filename = filename;
		this.srvAddress = srvAddress;
	}

	void sendFile() {
		try {

			// socket = new MyDatagramSocket();
			socket = new DatagramSocket();

			// create producer/consumer queue for ACKs
			receiverQueue = new ArrayBlockingQueue<>(1);
			// for statistics
			stats = new Stats();

			// start a receiver process to feed the queue
			new Thread(() -> {
				try {
					for (;;) {
						byte[] buffer = new byte[Ftp17Packet.MAX_FTP17_PACKET_SIZE];
						DatagramPacket msg = new DatagramPacket(buffer, buffer.length);
						socket.receive(msg);
						// update server address (it may change due to reply to UPLOAD coming from a different port
						srvAddress = msg.getSocketAddress();
						System.err.println(new Ftp17Packet(msg.getData(), msg.getLength()) + "from:" + msg.getSocketAddress() );
						// make the packet available to sender process
						Ftp17Packet pkt = new Ftp17Packet(msg.getData(), msg.getLength());
						receiverQueue.put(pkt);
					}
				} catch (Exception e) {
				}
			}).start();

			System.out.println("\nsending file: \"" + filename + "\" to server: " + srvAddress + " from local port:"
					+ socket.getLocalPort() + "\n");

			sendRetry(ACK, new UploadPacket(filename), 1L, DEFAULT_MAX_RETRIES);

			try {
				FileInputStream f = new FileInputStream(filename);
				long nextByte = 1L; // block byte count starts at 1
				// read and send blocks
				int n;
				byte[] buffer = new byte[BlockSize];
				while ((n = f.read(buffer)) > 0) {
					sendRetry(ACK, new DataPacket(nextByte, buffer, n), nextByte + n, DEFAULT_MAX_RETRIES);
					nextByte += n;
					stats.newPacketSent(n);
				}
				// FIN / FINACK exchange
				sendRetry(FINACK, new FinPacket(nextByte), nextByte, DEFAULT_MAX_RETRIES);
				f.close();

			} catch (Exception e) {
				System.err.println("failed with error \n" + e.getMessage());
				System.exit(0);
			}
			socket.close();
			System.out.println("Done...");
		} catch (Exception x) {
			x.printStackTrace();
			System.exit(0);
		}
		stats.printReport();
	}

	/*
	 * Send a block to the server, repeating until the expected ACK is received, or
	 * the number of allowed retries is exceeded.
	 */
	void sendRetry(short expectedOpCode, Ftp17Packet pkt, long expectedACK, int retries) throws Exception {
		for (int i = 0; i < retries; i++) {
			System.err.println("sending: " + pkt);
			long sendTime = System.currentTimeMillis();

			socket.send( pkt.toDatagram( srvAddress ));

			Ftp17Packet ack = receiverQueue.poll(Timeout, TimeUnit.MILLISECONDS);
			if (ack != null)
				if (ack.getOpcode() == expectedOpCode)
					if (expectedACK == ack.getSeqN()) {
						stats.newTimeoutMeasure(System.currentTimeMillis() - sendTime);
						System.err.println("got expected ack: " + expectedACK);
						return;
					} else {
						System.err.println("got wrong ack");
					}
				else {
					System.err.println("got unexpected packet (error)");
				}
			else
				System.err.println("timeout...");
		}
		throw new IOException("too many retries");
	}

	class Stats {
		private long totalRtt = 0;
		private int timesMeasured = 0;
		private int window = 1;
		private int totalPackets = 0;
		private int totalBytes = 0;
		private long startTime = 0L;;

		Stats() {
			startTime = System.currentTimeMillis();
		}

		void newPacketSent(int n) {
			totalPackets++;
			totalBytes += n;
		}

		void newTimeoutMeasure(long t) {
			timesMeasured++;
			totalRtt += t;
		}

		void printReport() {
			// compute time spent receiving bytes
			int milliSeconds = (int) (System.currentTimeMillis() - startTime);
			float speed = (float) (totalBytes * 8.0 / milliSeconds / 1000); // M bps
			float averageRtt = (float) totalRtt / timesMeasured;
			System.out.println("\nTransfer stats:");
			System.out.println("\nFile size:\t\t\t" + totalBytes);
			System.out.println("Packets sent:\t\t\t" + totalPackets);
			System.out.printf("End-to-end transfer time:\t%.3f s\n", (float) milliSeconds / 1000);
			System.out.printf("End-to-end transfer speed:\t%.3f M bps\n", speed);
			System.out.printf("Average rtt:\t\t\t%.3f ms\n", averageRtt);
			System.out.printf("Sending window size:\t\t%d packet(s)\n\n", window);
		}
	}

	public static void main(String[] args) throws Exception {
		// MyDatagramSocket.init(1, 1);
		try {
			switch (args.length) {
			case 5:
				// Ignored for S/W Client
				// WindowSize = Integer.parseInt(args[4]);
			case 4:
				BlockSize = Integer.valueOf(args[3]);
				// BlockSize must be at least 1
				if ( BlockSize <= 0 ) BlockSize = DEFAULT_BLOCK_SIZE;
				// for S/W Client WindowSize is equal to BlockSize
				WindowSize = BlockSize;
			case 3:
				Timeout = Integer.valueOf(args[2]);
				// Timeout must be at least 1 ms
				if ( Timeout <= 0 ) Timeout = 1;
			case 2:
				break;
			default:
				throw new Exception("bad parameters");
			}
		} catch (Exception x) {
			System.out.printf("usage: java Ftp17Client filename server [ timeout [ blocksize [ windowsize ]]]\n");
			System.exit(0);
		}
		String filename = args[0];
		String server = args[1];
		SocketAddress srvAddr = new InetSocketAddress(server, FTP17_PORT);
		new Ftp17ClientSW(filename, srvAddr).sendFile();
	}

	static class UploadPacket extends Ftp17Packet {
		UploadPacket(String filename) {
			putShort(UPLOAD);
			putLong(0L);
			putBytes(DUMMY_SCRATCHPAD);
			putString(filename);
		}
	}

	static class DataPacket extends Ftp17Packet {
		DataPacket(long seqN, byte[] payload, int length) {
			putShort(DATA);
			putLong( seqN );
			putBytes( DUMMY_SCRATCHPAD );
			putBytes(payload, length);
		}
	}
	
	static class FinPacket extends Ftp17Packet {
		FinPacket(long seqN) {
			putShort(FIN);
			putLong(seqN);
			putBytes( DUMMY_SCRATCHPAD );
		}
	}


} 
