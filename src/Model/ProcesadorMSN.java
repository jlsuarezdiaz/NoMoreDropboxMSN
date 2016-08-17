/*

 */
package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
class ProcesadorMSN extends Thread{
    /**
     * Server monitor.
     */
    private ServerData serverData;
    
    /**
     * Socket for communication with client.
     */
    private MSNSocket serviceSocket;
    
    
    /**
     * Indicates whether this processor thread is still alive.
     */
    private boolean running;
    
    /**
     * Constructir.
     * @param socketServicio Java socket to client.
     * @param s ServerData monitor.
     */
    public ProcesadorMSN(Socket socketServicio,ServerData s) {
        try {
            this.serviceSocket = new MSNSocket(socketServicio);
        }
        catch(Exception ex){
            Tracer.getInstance().trace(ex);
        }
        this.serverData=s;
        this.running=true;
    }
    
    /**
     * Processor method.
     */
    private void procesa() {
        CSMessage receivedData;
        CSMessage sendData = null;
        int remoteId = -1;
        
        //Nueva conexión. Enviamos el mensaje de saludo.
        Tracer.getInstance().trace(2,"New connection.");
        try {
            serviceSocket.writeMessage(new CSMessage(MessageKind.HELO,null));
        } catch (Exception ex) {
            Tracer.getInstance().trace(ex);
        }
             
        do{
            try {
                Tracer.getInstance().trace(2,"Thread "+remoteId+" waiting for message...");
                                
                receivedData = serviceSocket.readMessage();
                sendData = null;
                
                Tracer.getInstance().trace(receivedData);
                
                switch(receivedData.getMessageKind()){
                    case LOGIN: 
                        int id = serverData.addUser((String)receivedData.getData(0),this);    //Necesita Mutex
                        remoteId=id;
                        break;
                    case SEND: 
                    case SEND_FILE:
                        serverData.sendMessage(remoteId,receivedData);                      
                        break;
                    case CHANGE_PRIV: 
                        boolean state1 = serverData.changePrivate(remoteId);
                        sendData=new CSMessage(MessageKind.OK_PRIV,new Object[]{state1});
                        break;

                    case CHANGE_SLCT:
                        int chgId = (int)receivedData.getData(0);
                        boolean state2 = serverData.changeSelect(remoteId, chgId);
                        sendData=new CSMessage(MessageKind.OK_SLCT,new Object[]{chgId,state2});
                        break;
                    case CHANGE_STATE:
                        UserState usrState = serverData.changeState(remoteId,(UserState)receivedData.getData(0));
                        sendData=new CSMessage(MessageKind.OK_STATE,new Object[]{usrState});
                        break;
                    case LOGOUT:   
                        if(remoteId >= 0) serverData.removeUser(Integer.valueOf(remoteId));
                        else{
                            serviceSocket.writeMessage(new CSMessage(MessageKind.BYE, null));
                            kill();
                        }
                        break;

                    case IMALIVE: 
                        serverData.updateUser(remoteId);
                        break; 
                        
                    case VERSION:
                        double clientVersion = (double) receivedData.getData(0);
                        if(clientVersion < Data.Txt.LAST_COMPATIBLE){
                            //ERR_NEDDUPDATE, Info, OptYes, OptNo
                            sendData=new CSMessage(MessageKind.ERR_NEEDUPDATE,
                                new Object[]{"Necesita actualizar NoMoreDropboxMSN a su versión más reciente para poder seguir utilizándolo.",
                                "Actualizar","Salir",}
                            );
                        }
                        else if(clientVersion < Data.Txt.VERSION_CODE){
                            //WARN_NOTUPDATED, Info, OptYes, OptNo, canContinue
                            sendData=new CSMessage(MessageKind.WARN_NOTUPATED,
                                new Object[]{"Hay una versión más reciente disponible de NoMoreDropboxMSN. ¿Desea actualizar?",
                                "Actualizar","No actualizar"}
                            );
                        }
                        else{
                            sendData=new CSMessage(MessageKind.OK_VERSION,null);
                        }
                        break;
                    case FILE:
                        serverData.sendToAll(receivedData);
                        break;
                    case UPDATE_DOWNLOAD:
                        sendJarFile();
                        break;
                     /*
                    case UPDATE:
                        //System.out.println("["+info[1]+"] UPDATE received.");
                        if(!Server.isThereJarFile()){
                            datosEnviar=new Message(MessageKind.ERR,
                                new String[]{"No se puede descargar la actualización."}).toMessage();
                        }
                        else{
                            File f = new File("./NoMoreDropboxMSN.jar");
                            byte[] data = Files.readAllBytes(f.toPath());
                            
                            //FILE, Extensión, data
                            datosEnviar=new Message(MessageKind.FILE,new String[]{"jar",dataStr}).toMessage();
                            System.out.println(dataStr.length() + " bloques enviados.");
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
                    */
                    case NOP:
                        break;
                    default:
                        Tracer.getInstance().trace(new Exception("Error: Wrong command received: "+ receivedData.getMessageKind()));
                        sendData=new CSMessage(MessageKind.ERR_BADREQUEST,new Object[]{"El código de mensaje es incorrecto."});
                        break;
                };

                if(sendData!=null){
                    serviceSocket.writeMessage(sendData);
                }

            } catch (Exception ex) {
                Tracer.getInstance().trace(ex);
                if(!serviceSocket.isConnectionAlive()){
                    kill();
                }
            }
        }while(running);
        Tracer.getInstance().trace(1, "Connection ended.");
    }

    /**
     * Method to send a program update to the client.
     */
    private void sendJarFile() {
        try{
            File f = new File("./NoMoreDropboxMSN.jar");
            
            if(f.exists()){
                serviceSocket.writeMessage(new CSMessage(MessageKind.SEND_FILE, new Object[]{f.length()}));
                
                final int fileLengthSize = 50000;
                FileInputStream fis = new FileInputStream(f);
                int bytesRead = 0;
                long totalRead = 0;
                byte[] fileData = new byte[fileLengthSize];
                do{
                    bytesRead = fis.read(fileData);
                    CSMessage fileMsg = new CSMessage(MessageKind.FILE,
                       new Object[]{-1,-1,totalRead,bytesRead,fileData});
                    
                    if(bytesRead > 0){
                        totalRead+=bytesRead;
                        serviceSocket.writeMessage(fileMsg);
                    }
                    Tracer.getInstance().trace(2,Long.toString(totalRead)+" B sent.");
                }while(bytesRead >= 0);
                fis.close();
                
            }
            else{
                serviceSocket.writeMessage(new CSMessage(MessageKind.ERR_JARNOTFOUND,
                    new Object[]{"La actualización no está disponible para ser enviada."}));
            }
        }
        catch(Exception ex){
            Tracer.getInstance().trace(ex);
            try{
                serviceSocket.writeMessage(new CSMessage(MessageKind.ERR, new Object[]{ex.getMessage()}));
            }
            catch(Exception exx){ Tracer.getInstance().trace(exx); }
        }
    }
    
    //Añadimos el método run() de la clase Thread, con la función de procesamiento.
    public void run(){
        procesa();
    }
    
    public void kill(){
        try{
            serviceSocket.close();
        }
        catch(Exception ex){}
        this.running=false;
    }
    
    public MSNSocket getSocket(){
        return serviceSocket;
    }
    
}
