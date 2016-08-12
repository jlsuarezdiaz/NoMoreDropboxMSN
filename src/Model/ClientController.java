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
import static java.lang.Thread.sleep;
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
public class ClientController{
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
     * Timer for time out disconnections.
     */
    //private Timer timeOutOff;
    
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
        
        this.clientControllerInstance = this;
        sentMessages = new ArrayList();
        
    /*    timeOutOff = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.out.println("Timeoutoff timer activated.");
                clientState = ClientState.OFF;
                System.out.println("Timeoutoff timer ended.");
            }
        });*/
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
    
   
    /**
     * Reader thread to obtain server messages.
     */
    public void reader() {
        new Thread(new Runnable() {
        @Override
        public void run() {
            Tracer.getInstance().trace(1, "Reader thread started.");
            CSMessage receivedMsg = null;
            CSMessage sendMessage = null;
            do{
                try{
                    receivedMsg = mySocket.readMessage();
                }
                catch(Exception ex){
                    Tracer.getInstance().trace(ex);
                    receivedMsg = new CSMessage(MessageKind.NOP, null);
                    if(!mySocket.isConnectionAlive()){
                        disconnect();
                    }
                }
                sendMessage = null;
            
                Tracer.getInstance().trace(receivedMsg);
                
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
                                startLogin();
                                //sendMessage = new CSMessage(MessageKind.LOGIN, new Object[]{myUser.getName()});
                                break;
                            case WARN_NOTUPATED:
                                break;
                                
                            case ERR_NEEDUPDATE:
                                break;
                            case NOP:
                                break;
                            default:
                                Tracer.getInstance().trace(new Exception("Bad request: "+receivedMsg.getMessageKind()));
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
                                view.enableMSNComponents(true);
                                clientState = ClientState.ONLINE;
                                clientControllerInstance.updater.start();
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
                                Tracer.getInstance().trace(new Exception("Bad request: "+receivedMsg.getMessageKind()));
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
                                Message msg = (Message) receivedMsg.getData(0);
                                User sender = msg.getSender();
                                int msgNum = msg.getSeqNumber(); //Para enviar confirmación en el futuro.
                                
                                if(msg.getSender()!=null){
                                    if(msg.isPublic()){
                                        msg.addHeader(sender.getName() + " dice: ");
                                    }
                                    else{
                                        msg.addHeader("Mensaje privado de "+sender.getName()+": ");
                                    }
                                }
                                view.pushMessage(msg);
                                view.messageSound();
                            }
                                break;
                            case USERS:
                            {
                                clientControllerInstance.userList = (User[])receivedMsg.getData(0);
                            }
                                break;
                            case FILE:
                                
                                break;
                            case DISC:
                                disconnect();
                                break;
                            case KILL:
                                if(receivedMsg.getData() != null && receivedMsg.getData().length > 0){
                                    JOptionPane.showMessageDialog(view, receivedMsg.getData(0),"ERROR FATAL",JOptionPane.ERROR_MESSAGE);
                                }
                                System.exit(0);
                                break; 
                            case NOP:
                                break;
                            default:
                                Tracer.getInstance().trace(new Exception("Bad request: "+receivedMsg.getMessageKind()));
                                break;
                        }
                        view.setMSN();
                    }
                        break;
                    case DISCONNECTING:
                    {
                        switch(receivedMsg.getMessageKind()){
                            case BYE:
                                clientState = ClientState.OFF;
                                break;
                            case NOP:
                            default:
                                break;
                        }
                    }
                        break;
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
                                Tracer.getInstance().trace(new Exception("Bad request: "+receivedMsg.getMessageKind()));
                                break;
                        }
                    }
                        break;
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
        if(this.myUser==null){
            MSNIntro intro = new MSNIntro(view,true);
            String userName=null;              
            userName = intro.getUser();
            this.myUser = new User(userName);
        }
        sendToServer(new CSMessage(MessageKind.LOGIN, new Object[]{myUser.getName()}));
    }
    
    /**
     * Sends a communication message to the server.
     * @param msg CSMessage to send.
     */
    private void sendToServer(CSMessage msg){
        try{
            mySocket.writeMessage(msg);
        }
        catch(Exception ex){
            Tracer.getInstance().trace(ex);
        }                          
    }
    
    /**
     * Sends a user message to the server and other clients.
     * @param message Message to send.
     * @param isPrivate Indicates message scope. 
     */
    public void send(String message, boolean isPrivate){
        Message sendMsg = new Message(message, myUser, sentMessages.size(), !isPrivate);
        sentMessages.add(sendMsg);
        sendToServer(new CSMessage(MessageKind.SEND, new Object[]{sendMsg}));
    }
 /*   
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
*/
    /**
     * Changes client's private mode.
     */
    public void changePrivate(){
        CSMessage sendMsg = new CSMessage(MessageKind.CHANGE_PRIV,new Object[]{});
        sendToServer(sendMsg);
    }
    
    /**
     * Changes clients selection for a specific user.
     * @param idChange User id to change.
     */
    public void changeSelect(int idChange){
        selected[idChange]=!selected[idChange];
        CSMessage sendMsg = new CSMessage(MessageKind.CHANGE_SLCT,new Object[]{idChange});
        sendToServer(sendMsg);
    }
    
    /**
     * Checks whether a user is selected.
     * @param id User's id.
     * @return True, if and only if given user is selected.
     */
    public boolean isSelected(int id){
        return selected[id];
    }
    
    /**
     * Changes user state.
     * @param state New state. 
     */
    public void changeState(UserState state){
        if(clientState==ClientState.OFF){
            try {
                mySocket = new MSNSocket(Client.host, Client.port);
            } catch (IOException ex) {
                ex.printStackTrace();
                disconnect();
                return;
            }
            clientState=ClientState.START;
            reader();
            
            while(clientState!=ClientState.ONLINE){
                try {
                    sleep(100);
                } catch (InterruptedException ex) {}
            }
        }
        
        myUser.changeState(state);
        CSMessage sendMsg = new CSMessage(MessageKind.CHANGE_STATE,new Object[]{state});
        sendToServer(sendMsg);
    }
    
    public void stop() {
        if(clientState == ClientState.OFF) return;
        CSMessage sendMsg = new CSMessage(MessageKind.LOGOUT,new Object[]{});
        sendToServer(sendMsg);
        clientState = ClientState.DISCONNECTING;
        
        updater.stop();
        
        //Activamos un temporizador para cerrar si no obtenemos respuesta del servidor (timeout)
        
        
        //timeOutOff.start();
        
        
        //Esperamos a que el servidor dé el visto bueno para salir correctamente.
        //(hasta que el proceso lector no reciba un BYE del servidor.
        //Si no hay respuesta el temporizador dará paso.
        int timeOutCount = 0;
        while(clientState != clientState.OFF && timeOutCount < 300){
            try {
                sleep(10);
                timeOutCount++;
            } catch (InterruptedException ex) {}
        }
        
        clientState = ClientState.OFF;
        //timeOutOff.stop();
        view.enableMSNComponents(false);
        myUser.changeState(UserState.OFF);
        
        try {
            mySocket.close();
        } catch (Exception ex) {
            Tracer.getInstance().trace(ex);
        }
           
    }
    
    public void sendAliveMessage(){
        CSMessage sendMsg = new CSMessage(MessageKind.IMALIVE,new Object[]{});
        sendToServer(sendMsg);
        this.myUser.update();
    }
 
    /*
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
    }*/
    
    public void disconnect(){
        stop();
        view.setMSN();
        JOptionPane.showMessageDialog(this.view,"Te has desconectado.","Desconexión",JOptionPane.WARNING_MESSAGE);
        /*
        clientState = ClientState.OFF;
        if(myUser.getState() != UserState.OFF){
            this.myUser.changeState(UserState.OFF);
            this.view.setViewState(UserState.OFF);
            stop();
            JOptionPane.showMessageDialog(this.view,"Te has desconectado.","Desconexión",JOptionPane.WARNING_MESSAGE);
        }*/
    }
    
}
