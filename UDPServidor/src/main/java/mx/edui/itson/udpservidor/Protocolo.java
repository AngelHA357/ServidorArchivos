package mx.edui.itson.udpservidor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Diego Valenzuela Parra
 */
public class Protocolo implements Runnable {

    private DatagramSocket socketInfo;
    private DatagramSocket socketAck;

    public Protocolo(DatagramSocket socketInfo, DatagramSocket socketAck) {
        this.socketInfo = socketInfo;
        this.socketAck = socketAck;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[4096];
            DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);

            // Se espera la solicitud del cliente
            socketInfo.receive(requestPacket);
            String fileName = new String(requestPacket.getData(), 0, requestPacket.getLength());
            System.out.println("Solicitud de archivo: " + fileName);

            File file = new File(fileName);
            if (!file.exists()) {
                System.out.println("Archivo no encontrado.");
                socketInfo.close();
                return;
            }

            // Leer archivo en fragmentos
            try (FileInputStream fis = new FileInputStream(file)) {
                int bytesRead, packetNumber = 0;
                InetAddress clientAddress = requestPacket.getAddress();
                int clientPort = requestPacket.getPort();

                while ((bytesRead = fis.read(buffer)) != -1) {
                    String header = packetNumber + ";";
                    byte[] headerBytes = header.getBytes();
                    byte[] packetData = new byte[headerBytes.length + bytesRead];

                    // Se concatena el número de paquete con los datos para que el cliente pueda leer cada uno
                    System.arraycopy(headerBytes, 0, packetData, 0, headerBytes.length);
                    System.arraycopy(buffer, 0, packetData, headerBytes.length, bytesRead);

                    DatagramPacket sendPacket = new DatagramPacket(packetData, packetData.length, clientAddress, clientPort);
                    socketInfo.send(sendPacket);
                    packetNumber++;

                    // Se espera la confirmación del cliente (opcional, solo si quieres asegurar recepción)
                    DatagramPacket ackPacket = new DatagramPacket(new byte[10], 10);
                    socketInfo.setSoTimeout(100); // Evita que la espera del ACK bloquee la ejecución
                    try {
                        socketAck.receive(ackPacket);
                    } catch (IOException e) {
                        // Se ignora la excepción si no hay respuesta dentro del timeout
                    }
                }
            }

            // Se envía señal de fin
            byte[] endSignal = "END".getBytes();
            DatagramPacket endPacket = new DatagramPacket(endSignal, endSignal.length, requestPacket.getAddress(), requestPacket.getPort());
            socketInfo.send(endPacket);

            System.out.println("Archivo enviado con éxito.");
        } catch (IOException ex) {
            Logger.getLogger(Protocolo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
