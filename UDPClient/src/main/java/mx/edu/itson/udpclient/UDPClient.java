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
        
        System.out.println("Solicita el archivo que deseas (con su extensión como .pdf, .txt...");
        String requiredFile = sca.nextLine();
        
        byte[] bytesToSend = requiredFile.getBytes();
        
        DatagramPacket sendPacket = new DatagramPacket(bytesToSend, bytesToSend.length, InetAddress.getLocalHost(), 1001);
        udpSocket.send(sendPacket);
        
        FileOutputStream receivedFile = new FileOutputStream(requiredFile);
        byte[] bufferResponse = new byte[Integer.MAX_VALUE];
        
        while(true){
            DatagramPacket receivedPacket = new DatagramPacket(bufferResponse, bufferResponse.length);
            udpSocket.receive(receivedPacket);
            
            //Se verifica si se envía señal del fin del archivo
            String receivedData  = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
            if(receivedData.equals("END")){
                break;
            }
            
            receivedFile.write(receivedPacket.getData(), 0, receivedPacket.getLength());
        }
        
        System.out.println("Archivo recibido");
        receivedFile.close();
        udpSocket.close();
    }
}
