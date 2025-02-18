/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package mx.edui.itson.udpservidor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * @author JoseH
 */
public class UDPServidor {

    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket(1001);
        System.out.println("Servidor UDP esperando conexiones en el puerto " + 1001);

        byte[] buffer = new byte[4096];
        DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
        
        // Se espera la solicitud del cliente
        socket.receive(requestPacket);
        String fileName = new String(requestPacket.getData(), 0, requestPacket.getLength());
        System.out.println("Solicitud de archivo: " + fileName);

        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("Archivo no encontrado.");
            socket.close();
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
                socket.send(sendPacket);
                packetNumber++;

                // Se espera la confirmación del cliente
                DatagramPacket ackPacket = new DatagramPacket(new byte[10], 10);
                socket.receive(ackPacket);
            }
        }

        // Se envía señal de fin
        byte[] endSignal = "END".getBytes();
        DatagramPacket endPacket = new DatagramPacket(endSignal, endSignal.length, requestPacket.getAddress(), requestPacket.getPort());
        socket.send(endPacket);

        System.out.println("Archivo enviado con éxito.");
        socket.close();
    }
}
