package ftp17;

/**
 * Ftp17Server - File transfer protocol 2017 edition - RC FCT/UNL
 **/

import java.net.*;
import java.util.*;
import java.io.*;
import static ftp17.Ftp17Packet.*;



public class Ftp17Server implements Runnable {

	private static final int DEFAULT_WINDOW_SIZE = 1; // by default stop and wait

	static int DEFAULT_TRANSFER_TIMEOUT = 15000; // terminates transfer after
	// this timeout if no data block is received

	private Ftp17Packet request;
	private SocketAddress cltAddr;
	private int windowSize;

	Ftp17Server(int windowSize, Ftp17Packet request, SocketAddress cltAddr) {
		this.cltAddr = cltAddr;
		this.request = request;
		this.windowSize = windowSize;
	}

	public void run() {
		System.out.println("START!");
		receiveFile();
		System.out.println("DONE!");
	}
	
	static class ErrorPacket extends Ftp17Packet {
		ErrorPacket(String error) {
			putShort(ERROR);
			putLong(0L);
			putBytes(DUMMY_SCRATCHPAD);
			putString(error);
		}
	}

	static class AckPacket extends Ftp17Packet {
		AckPacket(long seqN, byte[] scratchpad, boolean discarded) {
			putShort(ACK);
			putLong(seqN);
			putBytes(scratchpad);
			putByte(discarded ? 1 : 0);
		}
	}

	static class FinAckPacket extends Ftp17Packet {
		FinAckPacket(long seqN, byte[] scratchpad) {
			putShort(FINACK);
			putLong(seqN);
			putBytes(scratchpad);
		}
	}

	private void receiveFile() {
		System.err.println("receiving file:" + request.getFilename());

		boolean finished = false;
		try (DatagramSocket socket = new DatagramSocket();
				RandomAccessFile raf = new RandomAccessFile("copy of " + request.getFilename(), "rw")) {

			// Defines the timeout to end the server, in case the client stops sending data
			socket.setSoTimeout(DEFAULT_TRANSFER_TIMEOUT);

			// confirms the file transfer request
			socket.send(new AckPacket(1L, request.getScratchPad(), false).toDatagram(cltAddr));

			SortedMap<Long, Integer> window = new TreeMap<>();

			long expectedByte = 1L; // next byte in sequence
			while (true) {
				byte[] buffer = new byte[MAX_FTP17_PACKET_SIZE];
				DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
				socket.receive(datagram);
				Ftp17Packet pkt = new Ftp17Packet(datagram.getData(), datagram.getLength());
				switch (pkt.getOpcode()) {
				case DATA:

					// is the data packet outside the window ?
					if (pkt.getSeqN() < expectedByte || pkt.getSeqN() > expectedByte + windowSize) {
						System.err.println("received packet out of window, ignoring...");
						socket.send(new AckPacket(expectedByte, pkt.getScratchPad(), true).toDatagram(cltAddr));
						continue;
					}
					byte[] data = pkt.getBlockData();
					
					// is this data packet a duplicate? if yes, sends an ACK as if it was correctly received
					if (window.putIfAbsent(pkt.getSeqN(), data.length) != null) {
						System.err.println("received a duplicate packet, ignoring...");
						socket.send(new AckPacket(expectedByte, pkt.getScratchPad(), false).toDatagram(cltAddr));
						continue;
					}

					// it is a new packet, save it, try to slide window and send an ACK
					raf.seek(pkt.getSeqN() - 1L);
					raf.write(data);

					// before sending the ack, try to slide window
					while (window.size() > 0 && window.firstKey() == expectedByte) {
						int size = window.remove(expectedByte);
						expectedByte += size;
					}
					socket.send(new AckPacket(expectedByte, pkt.getScratchPad(), false).toDatagram(cltAddr));
					break;
				case FIN:
					if (window.isEmpty() && expectedByte == pkt.getSeqN()) {
						socket.send(new FinAckPacket(pkt.getSeqN(), pkt.getScratchPad()).toDatagram(cltAddr));
						System.err.println("sent a FINACK packet since all file data was received ...");
						finished = true;
					} else
						socket.send(new AckPacket(expectedByte, pkt.getScratchPad(), true).toDatagram(cltAddr));
					break;
				case UPLOAD:
					socket.send(new AckPacket(1L, pkt.getScratchPad(), false).toDatagram(cltAddr));
					break;
				default:
					throw new RuntimeException("error receiving file." + request.getFilename() + ". Unexpected opcode: "
							+ pkt.getOpcode());
				}
			}
		} catch (SocketTimeoutException x) {
			if (!finished)
				System.err.printf("interrupted transfer; no data received after %s ms\n", DEFAULT_TRANSFER_TIMEOUT);
		} catch (Exception x) {
			System.err.println("receive failed: " + x.getMessage());
		}
	}

	public static void main(String[] args) throws Exception {

		int windowSize = args.length == 0 ? DEFAULT_WINDOW_SIZE : Integer.valueOf(args[0]);

		// create and bind socket to port for receiving client requests
		try (DatagramSocket mainSocket = new DatagramSocket(FTP17_PORT)) {

			System.out.println("New ftp17 server started at local port " + mainSocket.getLocalPort());
			for (;;) { // infinite processing loop...
				try {
					// receives request from clients
					byte[] buffer = new byte[MAX_FTP17_PACKET_SIZE];
					DatagramPacket msg = new DatagramPacket(buffer, buffer.length);
					mainSocket.receive(msg);
					// look at datagram as a SwFtpPacket
					Ftp17Packet req = new Ftp17Packet(msg.getData(), msg.getLength());
					switch (req.getOpcode()) {
					case UPLOAD: // Upload Request
						System.err.println("write request == receive file: " + req.getFilename());
						// Launch a dedicated thread to handle the client request...
						new Thread(new Ftp17Server(windowSize, req, msg.getSocketAddress())).start();
						break;
					default: // unexpected packet op code!
						mainSocket.send(new ErrorPacket("unexpected opcode: " + req.getOpcode() + " ignored\n")
								.toDatagram(msg.getSocketAddress()));
					}
				} catch (Exception x) {
					x.printStackTrace();
				}
			}
		}

	} // main

} // Ftp17Server
