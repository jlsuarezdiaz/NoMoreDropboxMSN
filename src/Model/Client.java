/*

 */
package Model;

import GUI.MSNIntro;
import GUI.MSNView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Juan Luis
 */
public class Client {
    public static final String host = "localhost";
    public static final int port = 8928;
    
    public static void main(String[] args){
        String buferEnvio;
        String buferRecepcion;
        
       
        
        Socket socketServicio = null;
        
        try{
            // Creamos un socket que se conecte a "host" y "port":
            //////////////////////////////////////////////////////
            socketServicio=new Socket(host,port);
            //////////////////////////////////////////////////////			
            Scanner inputStream=new Scanner(socketServicio.getInputStream(),"UTF-8");
            OutputStreamWriter outputStream=new OutputStreamWriter(socketServicio.getOutputStream(),"UTF-8");
            inputStream.useDelimiter(String.valueOf(ServerData.FS));

            
            buferRecepcion = inputStream.next();
            
            // Mostremos la cadena de caracteres recibidos:
            
            String[] info = buferRecepcion.split(String.valueOf(ServerData.GS));
            System.out.println("["+info[1]+"] "+info[0]+" received.");
            if(MessageKind.valueOf(info[0])!=MessageKind.HELO){
                System.err.println("No se obtuvo una respuesta correcta del servidor");
                System.exit(-1);
            }
            
            //Si el servidor nos da la bienvenida, cargamos la ventana del LOGIN:
            MSNView msn_view = new MSNView();
            MSNIntro intro = new MSNIntro(msn_view,true);
            String userName=null;
            do{
                

                userName = intro.getUser();
                
                buferEnvio = new Message(MessageKind.LOGIN, new String[]{userName}).toMessage();
                
 
                outputStream.write(buferEnvio);
                outputStream.flush();
                
                buferRecepcion = inputStream.next();
                info = buferRecepcion.split(String.valueOf(ServerData.GS));
                
                System.out.println("["+info[1]+"] "+info[0]+" received.");
                if(MessageKind.valueOf(info[0])==MessageKind.ERR){
                    JOptionPane.showMessageDialog(msn_view, info[2], "ERROR", JOptionPane.ERROR_MESSAGE);
                }
            }while(MessageKind.valueOf(info[0])==MessageKind.ERR);
            
            ClientController c = new ClientController(Integer.valueOf(info[2]), userName, msn_view, socketServicio,inputStream,outputStream);
            msn_view.setMSN(c);
            msn_view.showView();
      

            // Una vez terminado el servicio, cerramos el socket (automáticamente se cierran
            // el inpuStream  y el outputStream)
            //////////////////////////////////////////////////////
            
        } catch (IOException ex) {
            System.err.println("Error: no se pudo establecer una conexión con el servidor.");
        }
    }
}
