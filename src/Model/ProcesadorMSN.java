/*

 */
package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
class ProcesadorMSN extends Thread implements Communicator{
    //Referencia al servidor
    private ServerData serverData;
    
    // Referencia a un socket para enviar/recibir las peticiones/respuestas
    private Socket socketServicio;
    // stream de lectura (por aquí se recibe lo que envía el cliente)
    //private BufferedReader inputStream;
    private Scanner inputStream;
    // stream de escritura (por aquí se envía los datos al cliente)
    private OutputStreamWriter outputStream;
    
    /**
     * Indicates whether this processor thread is still alive.
     */
    private boolean running;
    
    
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
        this.running=true;
    }

    private void procesa() {
        String datosRecibidos;
        String datosEnviar = "";
        int remoteId = -1;
        
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
                System.out.println("Thread "+remoteId+" waiting for message...");
                //Leemos un nuevo mensaje.
                while(!inputStream.hasNext() && running){}
                if(!running) break;
                
                datosEnviar="";
                datosRecibidos=inputStream.next();
                String[] info = datosRecibidos.split(String.valueOf(ServerData.GS));
                System.out.println("["+info[1]+"] "+info[0]+" received.");
                
                switch(MessageKind.valueOf(info[0])){
                    case LOGIN: //Fecha,Nombre
                        //System.out.println("["+info[1]+"] LOGIN received.");
                        int id = serverData.addUser(info[2],outputStream,this);    //Necesita Mutex
                        if(id == -1){
                            datosEnviar = new Message(MessageKind.ERR,new String[]{"Hay demasiados usuarios conectados. Inténtelo más tarde."}).toMessage();
                        }
                        remoteId=id;
                        break;
                    case SEND:  //Fecha,ID, mensaje
                        //System.out.println("["+info[1]+"] SEND received.");
                        serverData.sendMessage(Integer.valueOf(info[2]),info[3]);                      
                        //datosEnviar = new Message(MessageKind.OK, null).toMessage();
                        break;
                    case CHANGEPRIVATE: //Fecha,ID
                        //System.out.println("["+info[1]+"] CHANGEPRIVATE received.");
                        boolean state1 = serverData.changePrivate(Integer.valueOf(info[2]));
                        //Mensaje de confirmación con el estado que se ha guardado en el servidor.
                        datosEnviar=new Message(MessageKind.CONFIRMPRV,new String[]{Boolean.toString(state1)}).toMessage();
                        break;

                    case CHANGESELECT: //Fecha,ID, UserChanged
                        //System.out.println("["+info[1]+"] CHANGESELECT received.");
                        boolean state2 = serverData.changeSelect(Integer.valueOf(info[2]),Integer.valueOf(info[3]));

                        datosEnviar=new Message(MessageKind.CONFIRMSLCT,new String[]{info[3],Boolean.toString(state2)}).toMessage();
                        break;
                    case CHANGESTATE: //Fecha,ID,State
                        //System.out.println("["+info[1]+"] CHANGESTATE received.");
                        UserState usrState = serverData.changeState(Integer.valueOf(info[2]),UserState.valueOf(info[3]));

                        datosEnviar=new Message(MessageKind.CONFIRMSTATE,new String[]{usrState.toString()}).toMessage();
                        break;
                    case LOGOUT:   //Fecha,ID
                        //System.out.println("["+info[1]+"] LOGOUT received.");
                        serverData.removeUser(Integer.valueOf(info[2]));
                        
                        kill();
                        break;

                    case IMALIVE: //Fecha, ID
                        //System.out.println("["+info[1]+"] IMALIVE received.");
                        serverData.updateUser(Integer.valueOf(info[2]));
                        break; 
                        
                    case VERSION: //Fecha, Version
                        //System.out.println("["+info[1]+"] VERSION received.");
                        double clientVersion = Double.valueOf(info[2]);
                        if(clientVersion < Data.Txt.LAST_COMPATIBLE){
                            //UPDATE, Info, OptYes, OptNo, canContinue
                            datosEnviar=new Message(MessageKind.UPDATE,
                                new String[]{"Necesita actualizar NoMoreDropboxMSN a su versión más reciente para poder seguir utilizándolo.",
                                "Actualizar","Salir",Boolean.toString(false)}
                            ).toMessage();
                        }
                        else if(clientVersion < Data.Txt.VERSION_CODE){
                            //UPDATE, Info, OptYes, OptNo, canContinue
                            datosEnviar=new Message(MessageKind.UPDATE,
                                new String[]{"Hay una versión más reciente disponible de NoMoreDropboxMSN. ¿Desea actualizar?",
                                "Actualizar","No actualizar",Boolean.toString(true)}
                            ).toMessage();
                        }
                        else{
                            datosEnviar=new Message(MessageKind.OK,null).toMessage();
                        }
                        break;
                    case UPDATE:
                        //System.out.println("["+info[1]+"] UPDATE received.");
                        if(!Server.isThereJarFile()){
                            datosEnviar=new Message(MessageKind.ERR,
                                new String[]{"No se puede descargar la actualización."}).toMessage();
                        }
                        else{
                            File f = new File("./NoMoreDropboxMSN.jar");
                            byte[] data = Files.readAllBytes(f.toPath());
                            /*String dataStr = new String(data,StandardCharsets.UTF_8);
                            //FILE, Extensión, data
                            datosEnviar=new Message(MessageKind.FILE,new String[]{"jar",dataStr}).toMessage();
                            System.out.println(dataStr.length() + " bloques enviados.");*/
                            serverData.sendFile(this, f.getName(), data, "SERVER");
                        }
                        break;
                    case FILE: //FILE, date, name, length, sender
                        //System.out.println("["+info[1]+"] FILE received.");
                        String fileName = info[2];
                        int fileLength = Integer.valueOf(info[3]);
                        String sender = info[4];
                        serverData.setActivity(true, remoteId);
                        File f = FileUtils.FileSend.receiveFileProtocol(this, fileName, fileLength, null);
                        if(remoteId != -1) serverData.sendFile(remoteId,fileName, f, sender);
                        serverData.setActivity(false, remoteId);
                        break;
                    case WAIT:
                        long time = Long.valueOf(info[2]);
                        try {
                            Thread.sleep(time);
                        } catch (InterruptedException ex) {}

                        break;
                    case NOP:
                        break;
                    default:
                        System.out.println("["+info[1]+"] Error: Wrong command received: "+ info[0]);
                        datosEnviar=new Message(MessageKind.ERR,new String[]{"El código de mensaje es incorrecto."}).toMessage();
                        break;
                };

                if(!datosEnviar.equals("")){
                    outputStream.write(datosEnviar);
                    outputStream.flush();
                }

            } catch (Exception ex) {
                //System.err.println("Error al obtener los flujos de entrada/salida.");      
                System.err.println("Error en el procesador:\n"+ ex.getMessage());
            }
        }while(true);
        System.out.println("["+Message.getDateFormat().format(new Date())+"] Connection ended.");

    }
    
    //Añadimos el método run() de la clase Thread, con la función de procesamiento.
    public void run(){
        procesa();
    }
    
    public void kill(){
        try{
            socketServicio.close();
        }
        catch(Exception ex){}
        this.running=false;
    }
    
    public Socket getSocket(){
        return socketServicio;
    }
    
    public OutputStream getOutputStream() throws IOException{
        return socketServicio.getOutputStream();
    }
    
    public OutputStreamWriter getOutputStreamWriter(){
        return outputStream;
    }
    
    public Scanner getInputScanner(){
        return inputStream;
    }
    
    public InputStream getInputStream() throws IOException{
        return socketServicio.getInputStream();
    }
    
}
