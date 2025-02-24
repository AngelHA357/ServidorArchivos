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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author JoseH
 */
public class UDPServidor {
    public static void main(String[] args) throws IOException {
        DatagramSocket socketInfo = new DatagramSocket(1001);
        DatagramSocket socketAck = new DatagramSocket(1002); // Creación única del socket de ACK
        System.out.println("Servidor UDP esperando conexiones en el puerto 1001");

        ExecutorService service = Executors.newFixedThreadPool(50); 

        while (true) {
            byte[] buffer = new byte[1024];
            DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
            socketInfo.receive(requestPacket); // Espera solicitud

            // Se crea un nuevo hilo solo cuando hay una solicitud
            service.execute(new Protocolo(requestPacket, socketAck));
        }
    }
}
