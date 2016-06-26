/*

 */
package Model;

import GUI.FileView;
import GUI.MSNView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 *
 * @author Juan Luis
 */
public class ClientController implements Communicator{
    /**
     * View associated to controller.
     */
    private MSNView view;
    
    private User myUser;
    
    private int myId;
    
    private Socket mySocket;
    
    private Scanner inputStream;
    
    private OutputStreamWriter outputStream;
    
    private Timer updater;
    
    //Periodo de actualización del cliente (en ms).
    private static final int UPDATE_TIME = 10000;
    
    //Indica si el cliente está conectado.
    private volatile boolean running;
    
    //COPY FROM SERVER LISTS
    private User[] userList = new User[User.getMaxUsers()];
    
    private boolean[] selected = new boolean[User.getMaxUsers()];
    
    private synchronized void setRunning(boolean b){
        this.running=b;
    }
     /* The reference for this instance of this object */
    private ClientController clientControllerInstance;

    ClientController(Integer id, String userName, MSNView msn_view, Socket userSocket, Scanner inputStream, OutputStreamWriter outputStream) {
        this.view = msn_view;
        this.myUser = new User(userName);
        this.myId = id;
        this.mySocket = userSocket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        setRunning(true);
        reader();//Iniciamos la hebra lectora.
        view.enableMSNComponents(running);
        
        this.updater = new Timer(UPDATE_TIME, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                sendAliveMessage();
            }
        });
        
        for(int i = 0; i < User.getMaxUsers(); i++){
            selected[i]=false;
        }
        this.updater.start();

        this.clientControllerInstance = this;
    }
    



    public User getUser() {
        return myUser;
    }

    public User[] getUserList() {
        return userList;
    }
    
    public int getMyId(){
        return myId;
    }
    
    //Actualiza la información de los usuarios con el mensaje recibido por el servidor.
    public void scanUsers(String userInfo){
        String [] userData = userInfo.split(String.valueOf(ServerData.RS));
        String [] userFields = null;
        for(int i = 0; i < User.getMaxUsers();i++){
            if(userData[i].equals("\0")){
                userList[i]=null;
            }
            else{
                userFields = userData[i].split(String.valueOf(ServerData.US));
                try{
                    userList[i] = new User(userFields[0],UserState.valueOf(userFields[1]),User.getDateFormat().parse(userFields[2]));
                }catch(Exception ex){
                    userList[i] = new User(userFields[0],UserState.valueOf(userFields[1]),null);                        
                }
            }
        }
        
    }

    //Hebra lectora de mensajes del servidor.
    public void reader() {
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Reader thread started.");
                String buferRecepcion = "";
                String info[] = null;
                do{
                    while(!inputStream.hasNext()){}
                    buferRecepcion = inputStream.next();
                    info = buferRecepcion.split(String.valueOf(ServerData.GS));
                    System.out.println("["+info[1]+"] "+info[0]+" received.");
                    switch(MessageKind.valueOf(info[0])){
                        case RECEIVEUSR:
                            scanUsers(info[2]);
                            view.setMSN();
                            break;
                        case RECEIVEMSG:
                            view.pushMessage(new Message(MessageKind.OK,new String[]{info[2]}));
                            view.messageSound();
                            break;
                        case CONFIRMPRV:
                            view.setPrivate(Boolean.valueOf(info[2]));
                            view.setMSN();
                            break;
                        case CONFIRMSLCT:
                            selected[Integer.valueOf(info[2])]=Boolean.valueOf(info[3]);
                            view.setMSN();
                            break;
                        case CONFIRMSTATE:
                            UserState state = UserState.valueOf(info[2]);
                            if(state != view.getViewState()){
                                view.setViewState(state);
                            }
                            break;
                        case OK:
                            if(info.length > 2){
                                view.pushMessage(new Message(MessageKind.OK,new String[]{info[2]}));
                            }
                            if(!updater.isRunning()) updater.start();
                            break;
                        case BYE:
                            setRunning(false);
                            break;
                        case DISC:
                            disconnect();
                            break;
                        case KILL:
                            if(info.length > 2){
                                JOptionPane.showMessageDialog(view, info[2],"ERROR FATAL",JOptionPane.ERROR_MESSAGE);
                            }
                            System.exit(0);
                            break;
                        case FILE: //FILE, date, name, length.
                            String name = info[2];
                            int length = Integer.valueOf(info[3]);
                            FileView fv = new FileView();
                            fv.setView(name,0 , length, "B", "Descargando");
                            view.pushFile(fv);
                            view.messageSound();
                            File f = FileUtils.FileSend.receiveFileProtocol(clientControllerInstance, name, length, fv);
                            fv.setFile(f);
                            break;
                        default:
                            System.err.println("Respuesta inadecuada. Mensaje ignorado.");
                            break;
                            
                    }
                    
                }while(running);
                
            }
        }).start();
        
    }
    
    private synchronized void sendToServer(String buferEnvio){
        try{
            outputStream.write(buferEnvio);
            outputStream.flush();
        }
        catch(Exception ex){
            System.out.println("Error al comunicarse con el servidor: "+ex.getMessage());
            disconnect();
        }
    }
    public void send(String message){
        String buferEnvio = new Message(MessageKind.SEND, new String[]{Integer.toString(myId),message}).toMessage();
        sendToServer(buferEnvio);
    }
    
    public synchronized void sendFile(File f){
        try{
            updater.stop();
            FileUtils.FileSend.sendFileProtocol(f,clientControllerInstance);
        }
        catch(Exception ex){
            System.err.println("Error: "+ex.getMessage());
            JOptionPane.showMessageDialog(view,"Error al enviar el archivo: "+
                    ex.getMessage() , "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void changePrivate(){
        String buferEnvio = new Message(MessageKind.CHANGEPRIVATE,new String[]{Integer.toString(myId)}).toMessage();
        sendToServer(buferEnvio);
    }
    
    public void changeSelect(int idChange){
        selected[idChange]=!selected[idChange];
        String buferEnvio = new Message(MessageKind.CHANGESELECT,new String[]{Integer.toString(myId),Integer.toString(idChange)}).toMessage();
        sendToServer(buferEnvio);
    }
    
    public boolean isSelected(int id){
        return selected[id];
    }
    
    public void changeState(UserState state){
        myUser.changeState(state);
        String buferEnvio = new Message(MessageKind.CHANGESTATE,new String[]{Integer.toString(myId),state.toString()}).toMessage();
        sendToServer(buferEnvio);
    }
    
    public void stop() {
        String buferEnvio = new Message(MessageKind.LOGOUT,new String[]{Integer.toString(myId)}).toMessage();
        sendToServer(buferEnvio);
        //Esperamos a que el servidor dé el visto bueno para salir correctamente.
        //(hasta que el proceso lector no reciba un BYE del servidor.
        updater.stop();
        
        while(running){}
        view.enableMSNComponents(running);
        myUser.changeState(UserState.OFF);
        try {
            mySocket.close();
        } catch (Exception ex) {
            System.err.println("Error al cerrar la comunicación: "+ex.getMessage());
        }
           
    }
    
    public void sendAliveMessage(){
        String buferEnvio = new Message(MessageKind.IMALIVE,new String[]{Integer.toString(myId)}).toMessage();
        sendToServer(buferEnvio);
        this.myUser.update();
    }
    
    public void restart(){
        try{
            mySocket = new Socket(Client.host, Client.port);
            inputStream=new Scanner(mySocket.getInputStream(),"UTF-8");
            outputStream=new OutputStreamWriter(mySocket.getOutputStream(),"UTF-8");
            inputStream.useDelimiter(String.valueOf(ServerData.FS));
            
            String buferRecepcion = inputStream.next();
            
            // Esperamos respuesta del servidor.
            
            String[] info = buferRecepcion.split(String.valueOf(ServerData.GS));
            System.out.println("["+info[1]+"] "+info[0]+" received.");
            if(MessageKind.valueOf(info[0])!=MessageKind.HELO){
                System.err.println("No se obtuvo una respuesta correcta del servidor");
                System.exit(-1);
            }
            
            //Enviamos petición de conexión.
            String buferEnvio = new Message(MessageKind.LOGIN,new String[]{myUser.getName()}).toMessage();
            sendToServer(buferEnvio);
            while(!inputStream.hasNext()){}
            buferRecepcion = inputStream.next();
            info = buferRecepcion.split(String.valueOf(ServerData.GS));
            if(MessageKind.valueOf(info[0]) == MessageKind.OK){
                this.myId = Integer.valueOf(info[2]);
                myUser.changeState(UserState.ONLINE);
                setRunning(true);
                reader();//Iniciamos la hebra lectora.
                view.enableMSNComponents(running);
                for(int i = 0; i < User.getMaxUsers(); i++){
                    selected[i]=false;
                }
                this.updater.start();
            }
            else{
                JOptionPane.showMessageDialog(view, "Error de conexión", info[2], JOptionPane.ERROR_MESSAGE);        
            }
        }
        catch(Exception ex){
            System.err.println("Error al restablecer la conexión: "+ex.getMessage());
            disconnect();
        }
    }
    
    public void disconnect(){
        running = false;
        if(myUser.getState() != UserState.OFF){
            this.myUser.changeState(UserState.OFF);
            this.view.setViewState(UserState.OFF);
            stop();
            JOptionPane.showMessageDialog(this.view,"Te has desconectado.","Desconexión",JOptionPane.WARNING_MESSAGE);
        }
    }
    
    // -- I/OPUT ACCESS -- //
    public InputStream getInputStream() throws IOException{
        return mySocket.getInputStream();
    }
    
    public OutputStream getOutputStream() throws IOException{
        return mySocket.getOutputStream();
    }
    
    public OutputStreamWriter getOutputStreamWriter(){
        return outputStream;
    }
    
    public Scanner getInputScanner(){
        return inputStream;
    }
    
    public Socket getSocket(){
        return mySocket;
    }
}
