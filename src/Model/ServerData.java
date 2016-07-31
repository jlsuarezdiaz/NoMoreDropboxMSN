/*

 */
package Model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;

/**
 * Server Data class. A monitor class to manage server information.
 * @author Juan Luis
 */

public class ServerData {
    /**
     * Max users allowed.
     */
    private static final int MAX_USERS = User.getMaxUsers();
    
    /**
     * Users list in the messenger.
     */
    private User[] user_list = new User[MAX_USERS];
    
    /**
     * Processor threads for each client.
     */    
    private ProcesadorMSN processors[] = new ProcesadorMSN[MAX_USERS];
    
    /**
     * Current number of users.
     */
    private int numUsers;
    
    /**
     * Selected users matrix.
     */
    private boolean selectedUsers[][] = new boolean[MAX_USERS][MAX_USERS];
    
    /**
     * Private mode users list.
     */
    private boolean privateMode[] = new boolean[MAX_USERS];
    
        
    /**
     * Maximum period of inactivity available before removing a user (in s).
     */
    private static final int MAX_INACTIVE_PERIOD = 30;
    
    /**
     * User checker timer.
     */
    private Timer userChecker;
    
    /**
     * Period the server checks inactive users (in ms)
     */
    private static final int CHECKER_PERIOD = 30000;
    
        
    /**
     * Utility to get time difference between two dates.
     * @param d1
     * @param d2
     * @return Time difference (in seconds).
     */
    private static long getTimeDifference(Date d1, Date d2){
        long diff = d1.getTime() - d2.getTime();
        return diff/1000;
    }

    /**
     * Method to obtain the maximum number of users allowed.
     * @return Maximum number of users allowed.
     */
    public static int getMAX_USERS() {
        return MAX_USERS;
    }

    /**
     * Constructor.
     */
    public ServerData() {
        numUsers = 0;
        for(int i = 0; i < MAX_USERS; i++){
            user_list[i] = new User();
            privateMode[i] = false;
            for(int j = 0; j < MAX_USERS; j++){
                selectedUsers[i][j] = false;
            }
        }
        
        this.userChecker = new Timer(CHECKER_PERIOD, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                checkUsers();
            }
        });
        
        this.userChecker.start();
    }
    
    /**
     * @return Current number of users in the server.
     */
    public int getNumUsers(){
        return numUsers;
    }
    
    /**
     * @return Current list of users.
     */
    public User[] getUserList(){
        return user_list;
    }
  
    /**
     * @deprecated 
     * @return 
     */
    private String getUsersString(){
        String s = "";
        for(User u: user_list){
            if(u ==null||!u.validState()){
               // s+="\0"+ServerData.RS;
            }
            else{
              //  s+=u.toMessage();
            }
        }
        return s;
    }
    
    /**
     * Sends a communication message to every clients.
     * @param message CSMessage to send.
     */
    public void sendToAll(CSMessage message){
        for(int i = 0; i < MAX_USERS; i++){
            if(processors[i] != null){
                try{
                   processors[i].getSocket().writeMessage(message);
                }
                catch(Exception ex){
                    System.err.println("Error al enviar mensaje: "+ex.getMessage());
                }
            }
        }
    }
    
    /**
     * Send a communication message to the specified client.
     * @param id Client id to send.
     * @param message CSMessage to send.
     */
    public void sendTo(int id, CSMessage message){
        try{
            processors[id].getSocket().writeMessage(message);
        }
        catch(Exception ex){
            System.err.println("Error al enviar el mensaje: "+ex.getMessage());
        }
    }
    
    /**
     * Sends a communication message to the selected clients.
     * @param id Sender's id.
     * @param msg Message to send.
     */
    public void sendToSelected(int id,CSMessage msg){
        for(int j = 0; j < MAX_USERS; j++){
            if(selectedUsers[id][j] && processors[j] != null){
                try{
                    sendTo(j, msg);
                }
                catch(Exception ex){
                    System.err.println("Error al enviar el mensaje: "+ex.getMessage()); 
                }
            }
        }
    }
    
    /**
     * Send a user message to all users.
     * @param id  Sender's id
     * @param message CSMessage to send.
     * @pre message must be a SEND message.
     */
    public void sendMessageToAll(int id,CSMessage message){
        int seqNumber = ((Message)message.getData(1)).getSeqNumber();
        for(int i = 0; i < MAX_USERS; i++){
            if(processors[i] != null){
                try{
                   sendTo(i,message);
                   sendTo(id,new CSMessage(MessageKind.OK_SENT,new Object[]{user_list[i],seqNumber}));
                }
                catch(Exception ex){
                    System.err.println("Error al enviar mensaje: "+ex.getMessage());
                }
            }
        }
    }
    
    /**
     * Sends a user message to the selected users.
     * @param id Sender's id.
     * @param message CSMessage to send.
     * @pre message must be a SEND message.
     */
    public void sendMessageToSelected(int id, CSMessage message){
        int seqNumber = ((Message)message.getData(1)).getSeqNumber();
        for(int j = 0; j < MAX_USERS; j++){
            if(selectedUsers[id][j] && processors[j] != null){
                try{
                    sendTo(j, message);
                    sendTo(id,new CSMessage(MessageKind.OK_SENT,new Object[]{user_list[j],seqNumber}));
                }
                catch(Exception ex){
                    System.err.println("Error al enviar el mensaje: "+ex.getMessage()); 
                }
            }
        }
    }
    
    /**
     * Sends a user message.
     * @param id Sender's id
     * @param cmsg CSMessage to send.
     * @pre cmsg must be a SEND message.
     */
    public synchronized void sendMessage(int id, CSMessage cmsg){
        Message msg = ((Message)cmsg.getData(1));
        if(msg.isPublic()){
            sendMessageToAll(id, cmsg);
        }
        else{
            sendMessageToSelected(id, cmsg);
        }
    }

    /**
     * Adds a user to the server.
     * @param name User's name.
     * @param p Processor thread associated.
     * @return User's id. If no space is available, -1 is returned.
     */   
    public synchronized int addUser(String name, ProcesadorMSN p){
        if(numUsers==getMAX_USERS()){
            
            try {
                p.getSocket().writeMessage(new CSMessage(MessageKind.ERR_USEROVERFLOW,
                    new Object[]{"Hay demasiados usuarios conectados. Inténtelo más tarde."}));
            } catch (IOException ex) {}
            return -1;
        }
        else{
            for(int i = 0; i < ServerData.getMAX_USERS(); i++){
                if(!user_list[i].validState()){
                    user_list[i] = new User(name);
                    privateMode[i] = false;
                    processors[i] = p;
                    for(int j = 0; j < getMAX_USERS(); j++){
                        selectedUsers[i][j] = false;
                    }
                    numUsers++;
                    sendTo(i,new CSMessage(MessageKind.OK_LOGIN,new Object[]{i}));
                    sendToAll(new CSMessage(MessageKind.SEND,new Object[]{
                        new Message(name+" ha iniciado sesión.",null, -1, true)}));
                    sendToAll(new CSMessage(MessageKind.USERS, new Object[]{user_list}));
                    return i;
                }
            }
            return -1;
        }
    }
    
    /**
     * Changes private mode of a user in server.
     * @param id User to be changed.
     * @return New private mode.
     */
    public synchronized boolean changePrivate(int id){
        privateMode[id]=!privateMode[id];
        return privateMode[id];
    }
    
    /**
     * Modifies a selection in server.
     * @param id User to be changed.
     * @param idChange selection to be changed.
     * @return new selection.
     */
    public synchronized boolean changeSelect(int id, int idChange){
        selectedUsers[id][idChange] = !selectedUsers[id][idChange];
        return selectedUsers[id][idChange];
    }
    
    /**
     * Changes a user state in server.
     * @param id User to be changed.
     * @param state New state.
     * @return New state.
     */
    public synchronized UserState changeState(int id, UserState state) {
        user_list[id].changeState(state);
        //Notificamos a todos los usuarios el nuevo cambio de estado.
        sendToAll(new CSMessage(MessageKind.USERS, new Object[]{user_list}));
        return user_list[id].getState();
    }
    
    /**
     * Removes a user from the server.
     * @param id User to remove.
     */
    public synchronized void removeUser(int id){
        String name = user_list[id].getName();
        user_list[id] = new User();
        numUsers--;
        //Notificamos a todos los usuarios el nuevo cambio.
        sendToAll(new CSMessage(MessageKind.USERS, new Object[]{user_list}));
        sendToAll(new CSMessage(MessageKind.SEND,new Object[]{
            new Message(name+" se ha desconectado.",null, -1, true)}));    
        sendTo(id,new CSMessage(MessageKind.BYE,null));
        processors[id].kill();
        processors[id] = null;
    }
    
    /**
     * Updates a user in the server.
     * @param id User to update.
     */
    public synchronized void updateUser(int id){
        user_list[id].update();
        sendTo(id,new CSMessage(MessageKind.USERS, new Object[]{user_list}));
    }
    
    /**
     * User checking function. Destroys inactive users in server.
     */
    public synchronized void checkUsers(){
        Date d = new Date();
        String name = "";
        System.out.println("["+MSNDateFormat.getInstance().format(d)+"] USER CHECKING Started.");
        for(int i = 0; i < MAX_USERS; i++){
            User u = user_list[i];
            if(u.getDate() != null && (getTimeDifference(d,u.getDate()) > MAX_INACTIVE_PERIOD 
                    || u.getState() == UserState.OFF)){
                //Si un usuario no ha dado señales de vida en cierto tiempo lo eliminamos.
                sendTo(i, new CSMessage(MessageKind.DISC, null));
                removeUser(i);
                System.out.println("- USER "+ Integer.toString(i) +" KILLED.");
            }
        }
    }
    
    /*
    public synchronized void sendFile(int id, String name, File f, String sender){
        
        try{
            if(this.privateMode[id]){
                sendFileToSelected(id,name,FileUtils.FileSend.loadFile(f.getAbsolutePath()), sender);
            }
            else{
                sendFileToAll(name,FileUtils.FileSend.loadFile(f.getAbsolutePath()), sender);
                sendTo(id, new Message(MessageKind.OK, null).toMessage());
            }
        }
        catch(Exception ex){
            System.err.println("Error: No se pudo iniciar el envío del archivo. "
                +ex.getMessage());
        }
        
    }*/
    
    
    /*
        
    public synchronized void sendFile(ProcesadorMSN p, String name, byte[] data, String sender){
        FileUtils.FileSend.sendFile(p, data, name, null, sender);
    }
    
    public synchronized void sendFileToAll(String name, byte[] data, String sender){
        for(int i = 0; i < MAX_USERS; i++){
            if(processors[i] != null && user_list[i].validState()){
                FileUtils.FileSend.sendFile(processors[i], data, name, null, sender);
            }
        }
    }*/
/*    
    public synchronized void sendFileToSelected(int id, String name, byte[] data, String sender){
        for(int j = 0; j < MAX_USERS; j++){
            if(selectedUsers[id][j]){
                if(processors[j] != null && user_list[j].validState()){
                    FileUtils.FileSend.sendFile(processors[j], data, name, null, sender+" [MENSAJE PRIVADO]");
                }
            }
        }
    }
 */   

}
