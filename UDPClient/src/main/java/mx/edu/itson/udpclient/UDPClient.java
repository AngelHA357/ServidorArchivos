/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package mx.edu.itson.udpclient;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 *
 * @author JoseH
 */
public class UDPClient {
    public static void main(String[] args) throws IOException {
        DatagramSocket udpSocket = new DatagramSocket();
        Scanner sca = new Scanner(System.in);
        System.out.println("Solicita el archivo que deseas (con su extensión como .pdf, .txt...): ");
        String requiredFile = sca.nextLine();
        byte[] bytesToSend = requiredFile.getBytes();
        
        
        DatagramPacket sendPacket = new DatagramPacket(bytesToSend, bytesToSend.length, InetAddress.getLocalHost(), 1001);
        udpSocket.send(sendPacket);
        
        FileOutputStream receivedFile = new FileOutputStream(requiredFile);
        byte[] bufferResponse = new byte[4096 + 10];
        
        while (true) {
            DatagramPacket receivedPacket = new DatagramPacket(bufferResponse, bufferResponse.length);
            udpSocket.receive(receivedPacket);
            
            // Se verifica si es la señal END
            String potentialEOF = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
            if (potentialEOF.equals("END")) {
                break;
            }
            
            byte[] receivedData = receivedPacket.getData();
            int length = receivedPacket.getLength();
            
            // Encontrar el caracter ';' para separar la cabecera
            int headerEnd = -1;
            for (int i = 0; i < length; i++) {
                if (receivedData[i] == ';') {
                    headerEnd = i;
                    break;
                }
            }
            
            if (headerEnd != -1) {
                // Se muestra el número de cada paquete de datos
                String packetNumber = new String(receivedData, 0, headerEnd);
                System.out.println("Recibido paquete #" + packetNumber);
                
                // Se escriben los datos
                receivedFile.write(receivedData, headerEnd + 1, length - (headerEnd + 1));
            }
            
            // Se envía confirmación de recibido
            byte[] ack = "ACK".getBytes();
            DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, receivedPacket.getAddress(), receivedPacket.getPort());
            udpSocket.send(ackPacket);
        }
        
        System.out.println("Archivo recibido.");
        receivedFile.close();
        udpSocket.close();
    }
}
