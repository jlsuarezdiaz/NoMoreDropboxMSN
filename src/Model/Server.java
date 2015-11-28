/*
 * Author: Juan Luis Suárez Díaz
 * July, 2015
 * Music Player
 */
package Model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Juan Luis
 */
public class Server {
    
    
    
    public static void main(String args[]){
        ServerData serverData = new ServerData();
        
        //Puerto de escucha
        int port = 8989;
        
        //Declaraciones
	ServerSocket serverSocket = null;
        Socket socketServicio = null;
        
        try {
            // Abrimos el socket en modo pasivo, escuchando el en puerto indicado por "port"
            //////////////////////////////////////////////////
            serverSocket=new ServerSocket(port);
            //////////////////////////////////////////////////

            // Mientras ... siempre!
            do {

                    // Aceptamos una nueva conexión con accept()
                    /////////////////////////////////////////////////
                    try{
                            socketServicio=serverSocket.accept();
                    }
                    catch(IOException e){
                            System.err.println("Error: no se pudo aceptar la conexión solicitada");
                    }
                    //////////////////////////////////////////////////
            
                    ProcesadorMSN procesador = new ProcesadorMSN(socketServicio,serverData);
                    procesador.start();
            }while(true);
        } catch (IOException e) {
                    System.err.println("Error al escuchar en el puerto "+port);
        }
    }
}
