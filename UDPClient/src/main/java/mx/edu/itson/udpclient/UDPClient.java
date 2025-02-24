/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package mx.edu.itson.udpclient;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

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

        RandomAccessFile receivedFile = new RandomAccessFile(requiredFile, "rw");
        byte[] bufferResponse = new byte[8192 + 20]; // Buffer con espacio para la cabecera

        while (true) {
            DatagramPacket receivedPacket = new DatagramPacket(bufferResponse, bufferResponse.length);
            udpSocket.receive(receivedPacket);

            String potentialEOF = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
            if (potentialEOF.equals("END")) {
                break;
            }

            byte[] receivedData = receivedPacket.getData();
            int length = receivedPacket.getLength();

            // Encontrar el carácter ';' para separar la cabecera
            int headerEnd = -1;
            for (int i = 0; i < length; i++) {
                if (receivedData[i] == ';') {
                    headerEnd = i;
                    break;
                }
            }

            if (headerEnd != -1) {
                // Obtener número de paquete
                String packetNumberStr = new String(receivedData, 0, headerEnd);
                int packetNumber = Integer.parseInt(packetNumberStr.trim());

                // Escribir los datos en la posición correcta
                receivedFile.seek(packetNumber * 8192);
                receivedFile.write(receivedData, headerEnd + 1, length - (headerEnd + 1));

                // Enviar ACK
                byte[] ack = ("ACK" + packetNumber).getBytes();
                DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, receivedPacket.getAddress(), 1002);
                udpSocket.send(ackPacket);
            }
        }

        System.out.println("Archivo recibido.");
        receivedFile.close();
        udpSocket.close();
    }
}