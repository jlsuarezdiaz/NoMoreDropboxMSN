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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
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
    //private BufferedReader inputStream;
    private Scanner inputStream;
    // stream de escritura (por aquí se envía los datos al cliente)
    private OutputStreamWriter outputStream;
    
    private static final String IO_LIM = "\0";
    
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
            inputStream=new Scanner(socketServicio.getInputStream(),"UTF-8");
            outputStream=new OutputStreamWriter(socketServicio.getOutputStream(),"UTF-8");
            inputStream.useDelimiter("\0END\0");
            datosRecibidos=inputStream.next();
            String[] info = datosRecibidos.split(IO_LIM);
            
            switch(MessageKind.valueOf(info[0])){
                case HELO:
                    System.out.println("["+info[1]+"] HELO received.");
                    datosEnviar = new Message(MessageKind.OK, null).toMessage();
                    //datosEnviar += MessageKind.OK.toString();
                    break;
                case LOGIN: //Fecha,Nombre
                    System.out.println("["+info[1]+"] LOGIN received.");
                    int id = serverData.addUser(info[2]);    //Necesita Mutex
                    if(id == -1){
                        //datosEnviar += MessageKind.ERR.toString() + IO_LIM;
                        //datosEnviar += "Hay demasiados usuarios conectados. Inténtelo más tarde."+IO_LIM;
                        datosEnviar = new Message(MessageKind.ERR,new String[]{"Hay demasiados usuarios conectados. Inténtelo más tarde."}).toMessage();
                    }
                    else{
                        //datosEnviar += MessageKind.OK.toString() + IO_LIM + Integer.toString(id) + IO_LIM;
                        datosEnviar = new Message(MessageKind.OK,new String[]{Integer.toString(id)}).toMessage();
                    }
                    break;
                case SEND:  //Fecha,ID, mensaje
                    System.out.println("["+info[1]+"] SEND received.");
                    ArrayList<Socket> sendSockets = serverData.getSendSockets(Integer.valueOf(info[2]));
                    //NO CREO QUE ESTÉ BIEN ESTO
                    for(Socket s: sendSockets){
                        PrintWriter send = new PrintWriter(s.getOutputStream(),true);
                        send.print(info[3]);
                        send.flush();
                    }
                    datosEnviar = new Message(MessageKind.OK, null).toMessage();
                    break;
                case CHANGEPRIVATE: //Fecha,ID
                    System.out.println("["+info[1]+"] CHANGEPRIVATE received.");
                    boolean state1 = serverData.changePrivate(Integer.valueOf(info[2]));
                    //Mensaje de confirmación con el estado que se ha guardado en el servidor.
                    datosEnviar=new Message(MessageKind.OK,new String[]{Boolean.toString(state1)}).toMessage();
                    break;
                    
                case CHANGESELECT: //Fecha,ID, UserChanged
                    System.out.println("["+info[1]+"] CHANGESELECT received.");
                    boolean state2 = serverData.changeSelect(Integer.valueOf(info[2]),Integer.valueOf(info[3]));
                    
                    datosEnviar=new Message(MessageKind.OK,new String[]{Boolean.toString(state2)}).toMessage();
                    break;
                case CHANGESTATE: //Fecha,ID,State
                    System.out.println("["+info[1]+"] CHANGESELECT received.");
                    UserState usrState = serverData.changeState(Integer.valueOf(info[2]),UserState.valueOf(info[3]));
                    
                    datosEnviar=new Message(MessageKind.OK,new String[]{usrState.toString()}).toMessage();
                    break;
                case BYE:   //Fecha,ID
                    System.out.println("["+info[1]+"] BYE received.");
                    serverData.removeUser(Integer.valueOf(info[2]));
                    break;
                
                case IMALIVE: //Fecha, ID
                    System.out.println("["+info[1]+"] IMALIVE received.");
                    serverData.updateUser(Integer.valueOf(info[2]));
                    break;
                case DISCONNECT: //Fecha, ID
                    System.out.println("["+info[1]+"] DISCONNECT received.");
                    serverData.removeUser(Integer.valueOf(info[2]));
                    break;
                case RECONNECT:
                    System.out.println("["+info[1]+"] LOGIN received.");
                    int rid = serverData.addUser(info[2]);    //Necesita Mutex
                    if(rid == -1){
                        //datosEnviar += MessageKind.ERR.toString() + IO_LIM;
                        //datosEnviar += "Hay demasiados usuarios conectados. Inténtelo más tarde."+IO_LIM;
                        datosEnviar = new Message(MessageKind.ERR,new String[]{"Hay demasiados usuarios conectados. Inténtelo más tarde."}).toMessage();
                    }
                    else{
                        //datosEnviar += MessageKind.OK.toString() + IO_LIM + Integer.toString(id) + IO_LIM;
                        datosEnviar = new Message(MessageKind.OK,new String[]{Integer.toString(rid)}).toMessage();
                    }
                    break;
                default:
                    System.out.println("["+info[1]+"] Error: Wrong command received.");
                    datosEnviar=new Message(MessageKind.ERR,new String[]{"El código de mensaje es incorrecto."}).toMessage();
                    break;
            };
            
            outputStream.write(datosEnviar);
            outputStream.flush();
        
        } catch (IOException ex) {
            System.err.println("Error al obtener los flujos de entrada/salida.");      
        }

    }
    
    //Añadimos el método run() de la clase Thread, con la función de procesamiento.
    public void run(){
        procesa();
    }
}
