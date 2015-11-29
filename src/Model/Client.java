/*
 * Author: Juan Luis Suárez Díaz
 * July, 2015
 * Music Player
 */
package Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Juan Luis
 */
public class Client {
    public static void main(String[] args){
        String buferEnvio;
        String buferRecepcion;
        
        String host = "localhost";
        int port = 8989;
        
        Socket socketServicio = null;
        
        try{
            // Creamos un socket que se conecte a "host" y "port":
            //////////////////////////////////////////////////////
            socketServicio=new Socket(host,port);
            //////////////////////////////////////////////////////			
            buferEnvio=new Message(MessageKind.HELO, null).toMessage();
            Scanner inputStream=new Scanner(socketServicio.getInputStream(),"UTF-8");
            OutputStreamWriter outputStream=new OutputStreamWriter(socketServicio.getOutputStream(),"UTF-8");
            inputStream.useDelimiter("\0END\0");
            // Enviamos el array por el outputStream;
            //////////////////////////////////////////////////////
            outputStream.write(buferEnvio);
            outputStream.flush();
            //////////////////////////////////////////////////////
            
            buferRecepcion = inputStream.next();
            
            // Mostremos la cadena de caracteres recibidos:
            
            String[] info = buferRecepcion.split("\0");
            System.out.println("["+info[1]+"] "+info[0]+" received.");
            if(MessageKind.valueOf(info[0])!=MessageKind.OK){
                System.err.println("No se obtuvo una respuesta correcta del servidor");
                System.exit(-1);
            }

            // Una vez terminado el servicio, cerramos el socket (automáticamente se cierran
            // el inpuStream  y el outputStream)
            //////////////////////////////////////////////////////
            socketServicio.close();
        } catch (IOException ex) {
            System.err.println("Error: no se pudo establecer una conexión con el servidor.");
        }
    }
}
