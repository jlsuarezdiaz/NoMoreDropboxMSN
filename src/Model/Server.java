/*

 */
package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Scanner;

/**
 *
 * @author Juan Luis
 */
public class Server {
    private static final String configName = ".configs";
    
    //Puerto de escucha
    private static final int port = readPort();
    
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
            +"\n\tPORT\t. Indica el puerto por el que está escuchando el servidor.";
    
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
                                    for(int i = 1; i < args.length; i++){
                                        serverData.sendToAll(new Message(MessageKind.RECEIVEMSG,new String[]{args[i]}).toMessage());
                                    }
                                    break;
                                case "sendasserver":
                                case "sas":
                                    for(int i = 1; i < args.length; i++){
                                        serverData.sendToAll(new Message(MessageKind.RECEIVEMSG,new String[]{"Mensaje del Servidor de No More Dropbox MSN: "+ args[i]}).toMessage());
                                    }
                                    break;
                                case "notifyrestart":
                                case "nrst":
                                    serverData.sendToAll(new Message(MessageKind.RECEIVEMSG,new String[]{"El servidor va a reiniciarse pronto. Finaliza tus conversaciones y guarda con tiempo la información que desees."}).toMessage());
                                    break;
                                case "notifyclose":
                                case "ncls":
                                    serverData.sendToAll(new Message(MessageKind.RECEIVEMSG,new String[]{"El servidor va a cerrarse pronto. Finaliza tus conversaciones y guarda con tiempo la información que desees."}).toMessage());
                                    break;
                                case "port":
                                    System.out.println("El servidor está escuchando por el puerto "+Integer.toString(port));
                                    break;
                                case "help":
                                    System.out.println(Server.serverHelp);
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

    /////////////////////////////////////////////////////////////////////////////
    public static void main(String args[]){
        ServerData serverData = new ServerData();
        
        
        
        //Declaraciones
	ServerSocket serverSocket = null;
        Socket socketServicio = null;
        
        try {
            // Abrimos el socket en modo pasivo, escuchando el en puerto indicado por "port"
            //////////////////////////////////////////////////
            serverSocket=new ServerSocket(port);
            System.out.println("["+Message.getDateFormat().format(new Date())+"] Server started.");

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
                    System.err.println("Error al escuchar en el puerto "+port);
        }
    }
}
