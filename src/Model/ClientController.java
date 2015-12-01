/*
 * Author: Juan Luis Suárez Díaz
 * July, 2015
 * Music Player
 */
package Model;

import GUI.MSNView;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author Juan Luis
 */
public class ClientController {
    /**
     * View associated to controller.
     */
    private MSNView view;
    
    private User myUser;
    
    private int myId;
    
    private Socket mySocket;
    
    private Scanner inputStream;
    
    private OutputStreamWriter outputStream;
    
    //COPY FROM SERVER LISTS
    private User[] userList = new User[User.getMaxUsers()];

    ClientController(Integer id, String userName, MSNView msn_view, Socket userSocket, Scanner inputStream, OutputStreamWriter outputStream) {
        this.view = view;
        this.myUser = new User(userName);
        this.myId = id;
        this.mySocket = userSocket;
        //view.enableMSNComponents(running);

    }
    

    public void setState(UserState userState) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public User getUser() {
        return myUser;
    }

    public User[] getUserList() {
        return userList;
    }

    public void reader() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void stop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
