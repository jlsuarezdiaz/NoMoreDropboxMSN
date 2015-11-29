/*
 * Author: Juan Luis Suárez Díaz
 * July, 2015
 * Music Player
 */
package Model;

import java.net.Socket;
import java.util.ArrayList;

/**
 *
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
    
    int numUsers;
    
    private boolean selectedUsers[][] = new boolean[MAX_USERS][MAX_USERS];
    
    private boolean privateMode[] = new boolean[MAX_USERS];

    public static int getMAX_USERS() {
        return MAX_USERS;
    }

    public ServerData() {
        numUsers = 0;
        for(int i = 0; i < MAX_USERS; i++){
            user_list[i] = new User();
            privateMode[i] = false;
           
            for(int j = 0; j < MAX_USERS; j++){
                selectedUsers[i][j] = false;
            }
        }
    }

    private int getNumUsers() {
        return numUsers;
    }

    private void setNumUsers(int numUsers) {
        this.numUsers = numUsers;
    }

        
    public int addUser(String name){
        if(numUsers==getMAX_USERS()){
            return -1;
        }
        else{
            for(int i = 0; i < ServerData.getMAX_USERS(); i++){
                if(!user_list[i].validState()){
                    user_list[i] = new User(name);
                    privateMode[i] = false;
                    for(int j = 0; j < getMAX_USERS(); j++){
                        selectedUsers[i][j] = false;
                    }
                    numUsers++;
                    return i;
                }
            }
            return -1;
        }
    }
    
    public ArrayList<Socket> getSendSockets(int id){
        ArrayList<Socket> sendSocket = new ArrayList();
        if(!privateMode[id]){
            for(int i = 0; i < MAX_USERS; i++){
                if(user_list[i].validState()){
                    sendSocket.add(user_list[i].getUserSocket());
                }
            }
        }
        else{
            for(int i = 0; i < MAX_USERS; i++){
                if(selectedUsers[id][i]){
                    sendSocket.add(user_list[i].getUserSocket());
                }
            }
        }
        return sendSocket;
    }
    
    public boolean changePrivate(int id){
        privateMode[id]=!privateMode[id];
        return privateMode[id];
    }
    
    public boolean changeSelect(int id, int idChange){
        selectedUsers[id][idChange] = !selectedUsers[id][idChange];
        return selectedUsers[id][idChange];
    }
    
    UserState changeState(int id, UserState state) {
        user_list[id].changeState(state);
        return user_list[id].getState();
    }
    
    public void removeUser(int id){
        user_list[id] = new User();
    }
    
    public void updateUser(int id){
        user_list[id].update();
    }
    
}
