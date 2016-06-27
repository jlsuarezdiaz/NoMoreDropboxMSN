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
import javax.swing.Timer;

/**
 *
 * @author Juan Luis
 */
public class ServerData {
    //Delimiters for information in messages
    public static final char US = 0x1F; //Unit separator (each piece of information)
    public static final char RS = 0x1E; //Record separator (groups of same information)
    public static final char GS = 0x1D; //Group separator (groups of complete information)
    public static final char FS = 0x1C; //File separator  (ends a complete message)
    /**
     * Max users allowed.
     */
    private static final int MAX_USERS = User.getMaxUsers();
    
    /**
     * Users list in the messenger.
     */
    private User[] user_list = new User[MAX_USERS];
    
    private OutputStreamWriter outputStreams[] = new OutputStreamWriter[MAX_USERS];
    
    private ProcesadorMSN processors[] = new ProcesadorMSN[MAX_USERS];
    
    private int numUsers;
    
    private boolean selectedUsers[][] = new boolean[MAX_USERS][MAX_USERS];
    
    private boolean privateMode[] = new boolean[MAX_USERS];
    
    private boolean activityFlag[] = new boolean[MAX_USERS];
    
    /**
     * Maximum period of inactivity available before removing a user (in s).
     */
    private static final int MAX_INACTIVE_PERIOD = 30;
    
    private Timer userChecker;
    
    /**
     * Period the server checks inactive users.
     */
    private static final int CHECKER_PERIOD = 30000;
    
    /**
     * UserChecker semaphore count.
     */
    private int userCheckerWait;
    
    /**
     * Utility to get time difference between two dates.
     * @param d1
     * @param d2
     * @return 
     */
    private static long getTimeDifference(Date d1, Date d2){
        long diff = d1.getTime() - d2.getTime();
        return diff/1000;
    }

    public static int getMAX_USERS() {
        return MAX_USERS;
    }

    public ServerData() {
        numUsers = 0;
        for(int i = 0; i < MAX_USERS; i++){
            user_list[i] = new User();
            privateMode[i] = false;
            activityFlag[i] = false;
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
        
        userCheckerWait = 0;
        this.userChecker.start();
    }
    
    public int getNumUsers(){
        return numUsers;
    }
    
    public User[] getUserList(){
        return user_list;
    }
  
    private String getUsersString(){
        String s = "";
        for(User u: user_list){
            if(u ==null||!u.validState()){
                s+="\0"+ServerData.RS;
            }
            else{
                s+=u.toMessage();
            }
        }
        return s;
    }
    
    public synchronized void sendToAll(String message){
    /*    for(OutputStreamWriter o: outputStreams){
            if(o != null){
                try{
                    o.write(message);
                    o.flush();
                }
                catch(Exception ex){
                    System.err.println("Error al enviar mensaje: "+ex.getMessage());
                }
            }
        }*/
        for(int i = 0; i < MAX_USERS; i++){
            if(processors[i] != null){
                OutputStreamWriter o = processors[i].getOutputStreamWriter();
                try{
                    while(activityFlag[i]){ sleep(500); }
                    o.write(message);
                    o.flush();
                }
                catch(Exception ex){
                    System.err.println("Error al enviar mensaje: "+ex.getMessage());
                }
            }
        }
    }
    
    public synchronized void sendTo(int id, String message){
        try{
            outputStreams[id].write(message);
            outputStreams[id].flush();
        }
        catch(Exception ex){
            System.err.println("Error al enviar el mensaje: "+ex.getMessage());
        }
    }
    
    public synchronized void sendToSelected(int id, String message,String orig){
        for(int j = 0; j < MAX_USERS; j++){
            if(selectedUsers[id][j]){
                try{
                    outputStreams[j].write(message);
                    outputStreams[j].flush();
                    sendTo(id,new Message(MessageKind.OK,new String[]{ "Enviaste a "+user_list[j].getName()+": "+orig}).toMessage());
                }
                catch(Exception ex){
                    System.err.println("Error al enviar el mensaje: "+ex.getMessage()); 
                }
            }
        }
    }
    
    public synchronized void sendMessage(int id, String msg){
        String completeMessage = "";
        if(this.privateMode[id]){
            completeMessage = new Message(MessageKind.RECEIVEMSG,new String[]{"Mensaje privado de "+user_list[id].getName()+": "+msg}).toMessage();
            sendToSelected(id,completeMessage,msg);
        }
        else{
            completeMessage = new Message(MessageKind.RECEIVEMSG,new String[]{user_list[id].getName() + " dice: "+msg}).toMessage();
            sendToAll(completeMessage);
            sendTo(id, new Message(MessageKind.OK, null).toMessage());
        }
    }

        
    public synchronized int addUser(String name, OutputStreamWriter o,ProcesadorMSN p){
        if(numUsers==getMAX_USERS()){
            return -1;
        }
        else{
            for(int i = 0; i < ServerData.getMAX_USERS(); i++){
                if(!user_list[i].validState()){
                    user_list[i] = new User(name);
                    privateMode[i] = false;
                    outputStreams[i]= o;
                    processors[i] = p;
                    for(int j = 0; j < getMAX_USERS(); j++){
                        selectedUsers[i][j] = false;
                    }
                    numUsers++;
                    sendTo(i,new Message(MessageKind.OK,new String[]{Integer.toString(i)}).toMessage());
                    sendToAll(new Message(MessageKind.RECEIVEMSG,new String[]{name+" ha iniciado sesión."}).toMessage());
                    sendToAll(new Message(MessageKind.RECEIVEUSR, new String[]{getUsersString()}).toMessage());
                    return i;
                }
            }
            return -1;
        }
    }
    
    
    public synchronized boolean changePrivate(int id){
        privateMode[id]=!privateMode[id];
        return privateMode[id];
    }
    
    public synchronized boolean changeSelect(int id, int idChange){
        selectedUsers[id][idChange] = !selectedUsers[id][idChange];
        return selectedUsers[id][idChange];
    }
    
    public synchronized UserState changeState(int id, UserState state) {
        user_list[id].changeState(state);
        //Notificamos a todos los usuarios el nuevo cambio de estado.
        sendToAll(new Message(MessageKind.RECEIVEUSR, new String[]{getUsersString()}).toMessage());
        return user_list[id].getState();
    }
    
    public synchronized void removeUser(int id){
        String name = user_list[id].getName();
        user_list[id] = new User();
        numUsers--;
        //Notificamos a todos los usuarios el nuevo cambio.
        sendToAll(new Message(MessageKind.RECEIVEUSR, new String[]{getUsersString()}).toMessage());
        sendToAll(new Message(MessageKind.RECEIVEMSG,new String[]{name+" se ha desconectado."}).toMessage());
        sendTo(id,new Message(MessageKind.BYE,null).toMessage());
        outputStreams[id] = null;
        processors[id] = null;
    }
    
    public synchronized void updateUser(int id){
        user_list[id].update();
        sendToAll(new Message(MessageKind.RECEIVEUSR,new String[]{getUsersString()}).toMessage());
    }
    
    public synchronized void checkUsers(){
        Date d = new Date();
        String name = "";
        System.out.println("["+User.getDateFormat().format(d)+"] USER CHECKING Started.");
        for(int i = 0; i < MAX_USERS; i++){
            User u = user_list[i];
            if(!activityFlag[i] && u.getDate() != null && (getTimeDifference(d,u.getDate()) > MAX_INACTIVE_PERIOD 
                    || u.getState() == UserState.OFF)){
                //Si un usuario no ha dado señales de vida en cierto tiempo lo eliminamos.
                sendTo(i, new Message(MessageKind.DISC, null).toMessage());
                name = user_list[i].getName();
                user_list[i] = new User();
                outputStreams[i] = null;
                processors[i].kill();
                processors[i] = null;
                numUsers--;
                sendToAll(new Message(MessageKind.RECEIVEMSG,new String[]{name+" se ha desconectado."}).toMessage());
                sendToAll(new Message(MessageKind.RECEIVEUSR,new String[]{getUsersString()}).toMessage());
                System.out.println("- USER "+ Integer.toString(i) +" KILLED.");
            }
        }
    }
    
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
        
    }
    
    
    public synchronized void waitUserChecker(){
        userCheckerWait++;
        if(userCheckerWait > 0) userChecker.stop();
    }
    
    public synchronized void signalUserChecker(){
        userCheckerWait--;
        if(userCheckerWait <= 0){
            userCheckerWait = 0;
            if(!userChecker.isRunning()) userChecker.start();
        }
    }
        
    public synchronized void sendFile(ProcesadorMSN p, String name, byte[] data, String sender){
        FileUtils.FileSend.sendFile(p, data, name, null, sender);
    }
    
    public synchronized void sendFileToAll(String name, byte[] data, String sender){
        for(int i = 0; i < MAX_USERS; i++){
            if(processors[i] != null && user_list[i].validState()){
                FileUtils.FileSend.sendFile(processors[i], data, name, null, sender);
            }
        }
    }
    
    public synchronized void sendFileToSelected(int id, String name, byte[] data, String sender){
        for(int j = 0; j < MAX_USERS; j++){
            if(selectedUsers[id][j]){
                if(processors[j] != null && user_list[j].validState()){
                    FileUtils.FileSend.sendFile(processors[j], data, name, null, sender+" [MENSAJE PRIVADO]");
                }
            }
        }
    }
    
    public synchronized void setActivity(boolean b, int id){
        this.activityFlag[id]=b;
    }
}
