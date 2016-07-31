/*

 */
package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Thread.sleep;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.jar.*;

/**
 *
 * @author Juan Luis
 */
public class Server {
    private static final String configName = ".configs";
    
    //Puerto de escucha
    private static final int port = readPort();
    
    private static ServerSocket serverSocket = null;
    
    private static final int readPort(){
        int rPort = 8928; //Default port.
                String rHost = "localhost"; //Default host.
        File file = new File(configName);
        if(file.exists()){
            try{
                String txt = String.join("\n", Files.readAllLines(Paths.get(configName)));
                String[] data = txt.split("\n*(HOST\\s*=\\s*|PORT\\s*=\\s*)");
                rPort = Integer.valueOf(data[1]);
            }
            catch(Exception ex){}
        }
        return rPort;
    }
    ////////////////////////////////////////////////////////////////////////////
    private static final String serverHelp =
            "\nCOMANDOS DEL SERVIDOR DE NO MORE DROPBOX MSN:\n"
            +"\n\tEXIT\t- Cierra el servidor."
            +"\n\tSEND\t- Envía a todos los usuarios los mensajes que reciba de argumento."
            +"\n\tSENDASSERVER o SAS\t- Envía a todos los usuarios mensajes identificado como el servidor."
            +"\n\tNOTIFYRESTART o NRST\t- Avisa a los usuarios de un reinicio en breve del servidor."
            +"\n\tNOTIFYCLOSE o NCLS\t- Avisa a los usuarios del cierre en breve del servidor."
            +"\n\tUSERS\t- Indica cuántos usuarios hay conectados y su estado."
            +"\n\tPORT\t- Indica el puerto por el que está escuchando el servidor."
            +"\n\tADDRESS\t- Muestra información sobre las direcciones."
            +"\n\tCMD\t- Ejecuta comandos del sistema."
            +"\n\tDISC\t- Fuerza la desconexión del usuario con el ID indicado.";
    
    private static void reader(ServerData serverData){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner input = new Scanner(System.in,"UTF-8"); 
                input.useDelimiter("\n");
                while(true){
                    try{
                        String cmd = input.next();
                        String[] args = cmd.split("\\s+");
                        if(args.length > 0)
                            switch(args[0].toLowerCase()){
                                case "exit":
                                    System.out.println("Server will close now.");
                                    System.exit(0);
                                    break;
                                case "send":
                                    serverData.sendToAll(new CSMessage(MessageKind.SEND,new Object[]{
                                        new Message(cmd.split("^send\\s+")[1],null,-1, true)}));
                                    break;
                                case "sendasserver":
                                case "sas":
                                    serverData.sendToAll(new CSMessage(MessageKind.SEND,new Object[]{
                                        new Message("Mensaje del Servidor de No More Dropbox MSN: "+
                                                cmd.split("^(sendasserver|sas)\\s+")[1],null,-1,true)}));
                                    break;
                                case "notifyrestart":
                                case "nrst":
                                    serverData.sendToAll(new CSMessage(MessageKind.SEND,new Object[]{
                                        new Message("El servidor va a reiniciarse pronto. Finaliza tus conversaciones y guarda con tiempo la información que desees.",null,-1, true)}));
                                    break;
                                case "notifyclose":
                                case "ncls":
                                    serverData.sendToAll(new CSMessage(MessageKind.SEND,new Object[]{
                                        new Message("El servidor va a cerrarse pronto. Finaliza tus conversaciones y guarda con tiempo la información que desees.",null,-1, true)}));
                                    break;
                                case "port":
                                    System.out.println("El servidor está escuchando por el puerto "+Integer.toString(port));
                                    break;
                                case "address":
                                    System.out.println("Address =\t\t"+InetAddress.getLocalHost().getAddress());
                                    System.out.println("Canonical Host name =\t"+InetAddress.getLocalHost().getCanonicalHostName());
                                    System.out.println("Host Name =\t\t"+ InetAddress.getLocalHost().getHostName());
                                    System.out.println("Host Address =\t\t"+InetAddress.getLocalHost().getHostAddress());
                                    break;
                                case "whatsmyip":
                                case "wmip":
                                    //Solución provisional
                                    URL whatismyip = new URL("http://checkip.amazonaws.com");
                                    BufferedReader in = new BufferedReader(new InputStreamReader(
                                                    whatismyip.openStream()));

                                    String ip = in.readLine(); //you get the IP as a String
                                    System.out.println(ip);
                                    break;
                                case "help":
                                    System.out.println(Server.serverHelp);
                                    break;
                                case "users":
                                    System.out.println("USUARIOS CONECTADOS: "+serverData.getNumUsers());
                                    User[] ul = serverData.getUserList();
                                    for(int i = 0; i < User.getMaxUsers(); i++){
                                        if(ul[i]!= null && ul[i].validState()){
                                            System.out.println("\tID "+i+"\tNAME = "+ul[i].getName()+ "\tSTATE = "+ul[i].getState());
                                        }
                                    }
                                    break;
                                case "cmd":
                                    Process p = Runtime.getRuntime().exec(cmd.split("^cmd")[1]);
                                    p.waitFor(); 
                                    BufferedReader reader=new BufferedReader(
                                        new InputStreamReader(p.getInputStream())
                                    ); 
                                    String line; 
                                    while((line = reader.readLine()) != null) 
                                    { 
                                        System.out.println(line);
                                    } 
                                    break;
                                case "disc":
                                    try{
                                    if(args.length > 1){
                                        serverData.sendTo(Integer.valueOf(args[1]),new CSMessage(MessageKind.DISC, null));
                                    }
                                    else throw new IllegalArgumentException("Illegal arguments.");
                                    }
                                    catch(Exception ex){
                                        System.out.println("Uso: DISC id");
                                    }
                                    break;
                                case "versions":
                                    System.out.println("\nVERSIONES ANTERIORES\n");
                                    System.out.println(Data.Txt.OLD_VERSIONS_INFO);
                                    System.out.println("\nVERSIÓN ACTUAL\n");
                                    System.out.println(Data.Txt.LAST_VERSION_INFO);
                                    break;
                                    
                                case "client":
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Client.main(null);
                                        }
                                    }).start();
                                    
                                    System.out.println("Un nuevo cliente va a ser ejecutado.");
                                    System.out.println("¡ATENCIÓN! El cliente y el servidor pasan a formar parte "+
                                        "del mismo programa. El cierre de alguno cerrará ambos simultáneamente.");
                                    break;
                                case "kill":
                                    try{
                                    if(args.length > 2){
                                        serverData.sendTo(Integer.valueOf(args[1]),new CSMessage(MessageKind.KILL, new Object[]{cmd.split("^kill\\s+[0-9]+")[1]}));
                                    }
                                    else if(args.length > 1){
                                        serverData.sendTo(Integer.valueOf(args[1]),new CSMessage(MessageKind.KILL, null));                                       
                                    }
                                    else throw new IllegalArgumentException("Illegal arguments.");
                                    }
                                    catch(Exception ex){
                                        System.out.println("Uso: KILL id <mensaje opcional>");
                                    }
                                    break;
                                case "check":
                                    serverData.checkUsers();
                                    break;
                                case "":
                                    break;
                                default:
                                    System.out.println("Comando incorrecto. Utilice el comando HELP para más información.");
                                    break;
                            }
                    }catch(Exception ex){
                        System.err.println("Error al leer comando: "+ex.getMessage());
                    }
                }
            }
        }).start();
    }

    public static boolean isThereJarFile(){
        File f = new File("./NoMoreDropboxMSN.jar");
        return f.exists();
    }
    
    /////////////////////////////////////////////////////////////////////////////
    public static void main(String args[]){
        System.out.println(Data.Txt.EDITION + " Server");
        System.out.println("Un programa de " + Data.Txt.AUTHOR);
        System.out.println(Data.Txt.VERSION + "\t\t" + Data.Txt.COPYRIGHT);
        System.out.println("\n");
        
        try {
            sleep(3000);
        } catch (InterruptedException ex) {}
        
        if(!isThereJarFile()){
            System.err.println("El archivo .jar de este programa  no está disponible");
            System.err.println("El servidor no podrá enviar actualizaciones de software a los clientes.");
        }
        
        //Inicialización de los datos del servidor.
        ServerData serverData = new ServerData();   
        
        //Declaraciones
	ServerSocket serverSocket = null;
        Socket socketServicio = null;
        
        try {
            // Abrimos el socket en modo pasivo, escuchando el en puerto indicado por "port"
            //////////////////////////////////////////////////
            serverSocket=new ServerSocket(port);
            System.out.println("["+MSNDateFormat.getInstance().format(new Date())+"] Server started.");
            Server.serverSocket = serverSocket;
            //////////////////////////////////////////////////
            //Abrimos la hebra lectora del servidor, que permite la interactividad.
            reader(serverData);

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
                    System.err.println("Error al escuchar en el puerto "+port+"\n"+e.getMessage());
        }
    }
}
