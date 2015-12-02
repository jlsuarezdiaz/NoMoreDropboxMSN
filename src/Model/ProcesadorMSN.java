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
import java.util.Date;
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
    
    
    // Constructor que tiene como parámetro una referencia al socket abierto en por otra clase
    public ProcesadorMSN(Socket socketServicio,ServerData s) {
        this.socketServicio=socketServicio;
        this.serverData = s;
            
            // Obtiene los flujos de escritura/lectura
        try{
            inputStream=new Scanner(socketServicio.getInputStream(),"UTF-8");
            outputStream=new OutputStreamWriter(socketServicio.getOutputStream(),"UTF-8");
            inputStream.useDelimiter(String.valueOf(ServerData.FS));
        }
        catch(Exception ex){
            System.err.println("Error al crear el flujo E/S: "+ex.getMessage());
        }
    }

    private void procesa() {
        String datosRecibidos;
        String datosEnviar = "";
        
        //Nueva conexión. Enviamos el mensaje de saludo.
        System.out.println("["+Message.getDateFormat().format(new Date())+"] New connection.");
        try {
            outputStream.write(new Message(MessageKind.HELO,null).toMessage());
            outputStream.flush();
        } catch (IOException ex) {
            System.err.println("Error al saludar al cliente.");
        }
             
        do{
            try {
                //Leemos un nuevo mensaje.
                while(!inputStream.hasNext()){}
                datosEnviar="";
                datosRecibidos=inputStream.next();
                String[] info = datosRecibidos.split(String.valueOf(ServerData.GS));

                switch(MessageKind.valueOf(info[0])){
                    case LOGIN: //Fecha,Nombre
                        System.out.println("["+info[1]+"] LOGIN received.");
                        int id = serverData.addUser(info[2],outputStream);    //Necesita Mutex
                        if(id == -1){
                            datosEnviar = new Message(MessageKind.ERR,new String[]{"Hay demasiados usuarios conectados. Inténtelo más tarde."}).toMessage();
                        }
                        break;
                    case SEND:  //Fecha,ID, mensaje
                        System.out.println("["+info[1]+"] SEND received.");
                        serverData.sendMessage(Integer.valueOf(info[2]),info[3]);                      
                        //datosEnviar = new Message(MessageKind.OK, null).toMessage();
                        break;
                    case CHANGEPRIVATE: //Fecha,ID
                        System.out.println("["+info[1]+"] CHANGEPRIVATE received.");
                        boolean state1 = serverData.changePrivate(Integer.valueOf(info[2]));
                        //Mensaje de confirmación con el estado que se ha guardado en el servidor.
                        datosEnviar=new Message(MessageKind.CONFIRMPRV,new String[]{Boolean.toString(state1)}).toMessage();
                        break;

                    case CHANGESELECT: //Fecha,ID, UserChanged
                        System.out.println("["+info[1]+"] CHANGESELECT received.");
                        boolean state2 = serverData.changeSelect(Integer.valueOf(info[2]),Integer.valueOf(info[3]));

                        datosEnviar=new Message(MessageKind.CONFIRMSLCT,new String[]{info[3],Boolean.toString(state2)}).toMessage();
                        break;
                    case CHANGESTATE: //Fecha,ID,State
                        System.out.println("["+info[1]+"] CHANGESTATE received.");
                        UserState usrState = serverData.changeState(Integer.valueOf(info[2]),UserState.valueOf(info[3]));

                        datosEnviar=new Message(MessageKind.CONFIRMSTATE,new String[]{usrState.toString()}).toMessage();
                        break;
                    case LOGOUT:   //Fecha,ID
                        System.out.println("["+info[1]+"] LOGOUT received.");
                        serverData.removeUser(Integer.valueOf(info[2]));
                        datosEnviar=new Message(MessageKind.BYE,null).toMessage();
                        break;

                    case IMALIVE: //Fecha, ID
                        System.out.println("["+info[1]+"] IMALIVE received.");
                        serverData.updateUser(Integer.valueOf(info[2]));
                        break;                    
                    default:
                        System.out.println("["+info[1]+"] Error: Wrong command received.");
                        datosEnviar=new Message(MessageKind.ERR,new String[]{"El código de mensaje es incorrecto."}).toMessage();
                        break;
                };

                if(!datosEnviar.equals("")){
                    outputStream.write(datosEnviar);
                    outputStream.flush();
                }

            } catch (IOException ex) {
                System.err.println("Error al obtener los flujos de entrada/salida.");      
            }
        }while(true);

    }
    
    //Añadimos el método run() de la clase Thread, con la función de procesamiento.
    public void run(){
        procesa();
    }
}
