/*
 * Author: Juan Luis Suárez Díaz
 * July, 2015
 * Music Player
 */
package Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Juan Luis
 */
class ProcesadorMSN extends Thread{
    //Referencia al servidor
    private ServerData serverData;
    
    // Referencia a un socket para enviar/recibir las peticiones/respuestas
    private Socket socketServicio;
    // stream de lectura (por aquí se recibe lo que envía el cliente)
    private BufferedReader inputStream;
    // stream de escritura (por aquí se envía los datos al cliente)
    private PrintWriter outputStream;
    
    // Constructor que tiene como parámetro una referencia al socket abierto en por otra clase
    public ProcesadorMSN(Socket socketServicio,ServerData s) {
            this.socketServicio=socketServicio;
            this.serverData = s;
    }

    private void procesa() {
        String datosRecibidos;
        String datosEnviar = "";
        
        try {
            // Obtiene los flujos de escritura/lectura
            inputStream=new BufferedReader(new InputStreamReader(socketServicio.getInputStream()));
            outputStream=new PrintWriter(socketServicio.getOutputStream(),true);
            
            datosRecibidos=inputStream.readLine();
            String[] info = datosRecibidos.split("\0");
            
            switch(MessageKind.valueOf(info[0])){
                case HELO:
                    datosEnviar += MessageKind.OK.toString();
                    break;
                case LOGIN: //Nombre
                    int id = serverData.addUser(info[1]);    //Necesita Mutex
                    if(id == -1){
                        datosEnviar += MessageKind.ERR.toString() + "\0";
                        datosEnviar += "Hay demasiados usuarios conectados. Inténtelo más tarde.\0";
                    }
                    else{
                        datosEnviar += MessageKind.OK.toString() + "\0" + Integer.toString(id) + "\0";
                    }
                    break;
                case SEND:  //ID, mensaje
                    ArrayList<Socket> sendSockets = serverData.getSendSockets(Integer.valueOf(info[1]));
                    for(Socket s: sendSockets){
                        PrintWriter send = new PrintWriter(s.getOutputStream(),true);
                        send.print(info[2]);
                        send.flush();
                    }
                    datosEnviar += MessageKind.OK.toString() + "\0";
                    break;
                case CHANGEPRIVATE: //ID
                    boolean state1 = serverData.changePrivate(Integer.valueOf(info[1]));
                    //Mensaje de confirmación con el estado que se ha guardado en el servidor.
                    datosEnviar+=MessageKind.OK.toString() + "\0" + Boolean.toString(state1) + "\0";
                    break;
                    
                case CHANGESELECT: //ID, UserChanged
                    boolean state2 = serverData.changeSelect(Integer.valueOf(info[1]),Integer.valueOf(info[2]));
                    
                    datosEnviar += MessageKind.OK.toString() + "\0" + Boolean.toString(state2) + "\0";
                    break;
            };
            
        
        } catch (IOException ex) {
            System.err.println("Error al obtener los flujo de entrada/salida.");      
        }

    }
    
    //Añadimos el método run() de la clase Thread, con la función de procesamiento.
    public void run(){
        procesa();
    }
}
