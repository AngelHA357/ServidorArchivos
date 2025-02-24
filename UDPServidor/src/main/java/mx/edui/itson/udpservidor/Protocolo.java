package mx.edui.itson.udpservidor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Diego Valenzuela Parra
 */
public class Protocolo implements Runnable {

   private DatagramPacket requestPacket;
   private DatagramSocket socketAck;

    public Protocolo(DatagramPacket requestPacket, DatagramSocket socketAck) {
        this.requestPacket = requestPacket;
        this.socketAck = socketAck;
    }

    @Override
    public void run() {
        try (DatagramSocket socketInfo = new DatagramSocket()) {

            String fileName = new String(requestPacket.getData(), 0, requestPacket.getLength()).trim();
            System.out.println("Solicitud de archivo: " + fileName);

            File file = new File(fileName);
            if (!file.exists()) {
                System.out.println("Archivo no encontrado.");
                return;
            }

            InetAddress clientAddress = requestPacket.getAddress();
            int clientPort = requestPacket.getPort();

            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                int bytesRead, packetNumber = 0;
                byte[] dataBuffer = new byte[8192];

                while ((bytesRead = raf.read(dataBuffer)) != -1) {
                    String header = packetNumber + ";";
                    byte[] headerBytes = header.getBytes();
                    byte[] packetData = new byte[headerBytes.length + bytesRead];

                    System.arraycopy(headerBytes, 0, packetData, 0, headerBytes.length);
                    System.arraycopy(dataBuffer, 0, packetData, headerBytes.length, bytesRead);

                    DatagramPacket sendPacket = new DatagramPacket(packetData, packetData.length, clientAddress, clientPort);
                    socketInfo.send(sendPacket);

                    // Esperar ACK (uso sincronizado del socket compartido)
                    int attempts = 0;
                    while (attempts < 5) {
                        DatagramPacket ackPacket = new DatagramPacket(new byte[10], 10);
                        synchronized (socketAck) {
                            socketAck.setSoTimeout(100);
                            try {
                                socketAck.receive(ackPacket);
                            } catch (SocketTimeoutException e) {
                                attempts++;
                                continue;
                            }
                        }
                        String ackResponse = new String(ackPacket.getData(), 0, ackPacket.getLength()).trim();
                        if (ackResponse.equals("ACK" + packetNumber)) {
                            break;
                        } else {
                            attempts++;
                        }
                    }

                    packetNumber++;
                }
            }

            // Señal de fin de transmisión
            byte[] endSignal = "END".getBytes();
            DatagramPacket endPacket = new DatagramPacket(endSignal, endSignal.length, clientAddress, clientPort);
            socketInfo.send(endPacket);

            System.out.println("Archivo enviado con éxito.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
