/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package mx.edui.itson.udpservidor;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 *
 * @author JoseH
 */
public class UDPServidor {

    public static void main(String[] args) throws IOException {
        DatagramSocket socketInfo = new DatagramSocket(1001);
        DatagramSocket socketAck = new DatagramSocket(1002);
        System.out.println("Servidor UDP esperando conexiones en el puerto " + 1001);

        Executor service = Executors.newCachedThreadPool();

        while (true) {
            service.execute(new Protocolo(socketInfo, socketAck));
        }
    }
}
