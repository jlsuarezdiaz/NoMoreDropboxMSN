/*

 */
package Model;

import GUI.FileView;
import GUI.MSNIntro;
import GUI.MSNView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
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
    
    /**
     * Client user info.
     */
    private User myUser;
    
    /**
     * Client user id.
     */
    private int myId;
    
    /**
     * Client MSNSocket.
     */
    private MSNSocket mySocket;
    
    /**
     * User updater timer.
     */    
    private Timer updater;
    
    /**
     * Updating period (in ms).
     */
    private static final int UPDATE_TIME = 10000;
    
    /**
     * Client's state.
     */
    private volatile ClientState clientState;
    
    /**
     * List with current MSN users.
     */
    private User[] userList = new User[User.getMaxUsers()];
    
    /**
     * List of selected users.
     */
    private boolean[] selected = new boolean[User.getMaxUsers()];
    
    
    /* The reference for this instance of this object */
    private ClientController clientControllerInstance;
    
    /**
     * List of sent messages.
     */
    private ArrayList<Message> sentMessages;
    
    /**
     * Constructor.
     * @param userSocket MSNSocket to be used by the client.
     */
    ClientController(MSNSocket userSocket) {
        this.view = new MSNView();
        this.myUser = null;
        this.myId = -1;
        this.mySocket = userSocket;
        this.clientState = ClientState.START;
        
        reader();//Iniciamos la hebra lectora.
        //view.enableMSNComponents(running);
        
        this.updater = new Timer(UPDATE_TIME, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                sendAliveMessage();
            }
        });
        
        for(int i = 0; i < User.getMaxUsers(); i++){
            selected[i]=false;
        }
        //this.updater.start();
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

    /**
     * Reader thread to obtain server messages.
     */
    public void reader() {
        new Thread(new Runnable() {
        @Override
        public void run() {
            System.out.println("Reader thread started.");
            CSMessage receivedMsg = null;
            CSMessage sendMessage = null;
            do{
                try{
                    receivedMsg = mySocket.readMessage();
                }
                catch(Exception ex){
                    System.err.println("Error leyendo mensaje: "+ex.getMessage());
                    receivedMsg = new CSMessage(MessageKind.NOP, null);
                }
                sendMessage = null;
            /*    while(!inputStream.hasNext()){}
                buferRecepcion = inputStream.next();
                //System.out.println(buferRecepcion);//!!!!!!!!!!!//
                info = buferRecepcion.split(String.valueOf(ServerData.GS));
                System.out.println("["+info[1]+"] "+info[0]+" received.");*/

                switch(clientState){
                    case START: //Actions for state START
                    {
                        switch(receivedMsg.getMessageKind()){
                            case HELO: //After HELO we send VERSION.
                                sendMessage = new CSMessage(MessageKind.VERSION, new Object[]{Data.Txt.VERSION_CODE});
                                sendToServer(sendMessage);
                                break;
                                
                            case OK_VERSION: //Switch to LOGIN state.
                                clientState = ClientState.LOGIN;
                                if(myUser==null) startLogin();
                                sendMessage = new CSMessage(MessageKind.LOGIN, new Object[]{myUser.getName()});
                                break;
                            case WARN_NOTUPATED:
                                break;
                                
                            case ERR_NEEDUPDATE:
                                break;
                            case NOP:
                                break;
                            default:
                                System.err.println("Error: bad request: "+receivedMsg.getMessageKind());
                                break;
                        }
                    }
                        break;
                        
                    case LOGIN: //Actions for state LOGIN
                    {
                        switch(receivedMsg.getMessageKind()){
                            case OK_LOGIN:
                                clientControllerInstance.myId = (int) receivedMsg.getData(0);
                                view.setMSN(clientControllerInstance);
                                view.showView();
                                clientState = ClientState.ONLINE;
                                break;
                                
                            case ERR_USEROVERFLOW:
                                String err_msg = (String) receivedMsg.getData(0);
                                JOptionPane.showMessageDialog(view, err_msg, "ERROR: USER OVERFLOW", JOptionPane.ERROR_MESSAGE);
                                clientControllerInstance.myUser = null;
                                startLogin();
                                break;
                                
                            case NOP:
                                break;
                            default:
                                System.err.println("Error: bad request: "+receivedMsg.getMessageKind());
                                break;
                        }
                    }
                        break;
                        
                    case ONLINE:
                    {
                        switch(receivedMsg.getMessageKind()){
                            case OK_SENT:
                            {
                                User msgReceiver = (User) receivedMsg.getData(0);
                                int msgNum = (int) receivedMsg.getData(1);
                                Message sent = sentMessages.get(msgNum);
                                if(!sent.isPublic()){
                                    sent.setStatus(MessageStatus.SENT);
                                    sent.addHeader("Enviaste a "+msgReceiver.getName()+": ");
                                    view.pushMessage(sent);
                                    view.messageSound();
                                }
                            }
                                break;
                            case OK_PRIV:
                                view.setPrivate((boolean)receivedMsg.getData(0));
                                break;
                            case OK_SLCT:
                                selected[(int)receivedMsg.getData(0)]=(boolean)receivedMsg.getData(1);
                                break;
                            case OK_STATE:
                                myUser.changeState((UserState)receivedMsg.getData(0));
                                break;
                            case SEND:
                            {
                                User sender = (User) receivedMsg.getData(0);
                                int msgNum = (int) receivedMsg.getData(1); //Para enviar confirmación en el futuro.
                                Message msg = (Message) receivedMsg.getData(2);
                                if(msg.isPublic()){
                                    msg.addHeader(sender.getName() + " dice: ");
                                }
                                else{
                                    msg.addHeader("Mensaje privado de "+sender.getName()+": ");
                                }
                                view.pushMessage(msg);
                                view.messageSound();
                            }
                                break;
                            case FILE:
                                
                                break;
                            case NOP:
                                break;
                            default:
                                System.err.println("Error: bad request: "+receivedMsg.getMessageKind());
                                break;
                        }
                        view.setMSN();
                    }
                    case DISCONNECTING:
                    {
                        switch(receivedMsg.getMessageKind()){
                            case BYE:
                                clientState = ClientState.OFF;
                                break;
                            case NOP:
                                break;
                            default:
                                System.err.println("Error: bad request: "+receivedMsg.getMessageKind());
                                break;
                        }
                    }
                    case UPDATE:
                    {
                        switch(receivedMsg.getMessageKind()){
                            case FILE:
                                
                                break;
                            case ERR:
                                
                                break;
                            case ERR_JARNOTFOUND:
                                
                                break;
                            case NOP:
                                break;
                            default:
                                System.err.println("Error: bad request: "+receivedMsg.getMessageKind());
                                break;
                        }
                    }
                    default:
                        break;
                }


/*
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
                    case FILE: //FILE, date, name, length, sender
                        String name = info[2];
                        String sender = info[4];
                        int length = Integer.valueOf(info[3]);

                        FileView fv = new FileView();
                        fv.setView(name,0 , length, "B", "Descargando");
                        fv.setMetaView(info[1], sender);
                        view.pushFile(fv);
                        view.messageSound();
                        File f = FileUtils.FileSend.receiveFileProtocol(clientControllerInstance, name, length, fv);
                        fv.setFile(f);
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
                        System.err.println("Respuesta inadecuada. Mensaje ignorado.");
                        break;

                }*/

            }while(clientState != ClientState.OFF);

        }
        }).start();

    }
    
    /**
     * Performs login.
     */
    private void startLogin(){
        MSNIntro intro = new MSNIntro(view,true);
        String userName=null;              
        userName = intro.getUser();
        this.myUser = new User(userName);
        sendToServer(new CSMessage(MessageKind.LOGIN, new Object[]{userName}));
    }
    
    /**
     * Sends a message to the server.
     * @param msg Message to send.
     */
    private void sendToServer(CSMessage msg){
        try{
            mySocket.writeMessage(msg);
        }
        catch(Exception ex){
            System.err.println("Error al enviar mensaje: "+ex.getMessage());
        }
                                
    }
    public void send(String message){
        String buferEnvio = new Message(MessageKind.SEND, new String[]{Integer.toString(myId),message}).toMessage();
        sendToServer(buferEnvio);
    }
    
    public synchronized void sendFile(File f){
        new Thread(new Runnable() {

            @Override
            public void run() {
                
                try{
                    updater.stop();
                    FileView fv = new FileView();
                    fv.setView(f.getName(), 0, 0, "B", "Subiendo");
                    fv.setMetaView(User.getDateFormat().format(new Date()), myUser.getName());
                    view.pushFile(fv);
                    fv.setFile(f);
                    FileUtils.FileSend.sendFileProtocol(f,clientControllerInstance,fv,myUser.getName());
                }
                catch(Exception ex){
                    System.err.println("Error: "+ex.getMessage());
                    JOptionPane.showMessageDialog(view,"Error al enviar el archivo: "+
                            ex.getMessage() , "ERROR", JOptionPane.ERROR_MESSAGE);
                }
                //sendAliveMessage();
                
                
                
            }
        }).start();
        
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
