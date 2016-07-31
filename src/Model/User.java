////////////////////////////////////////////////////////////////////////////////
// Author: Juan Luis Suarez Diaz
// Jun, 2015
// Dropbox MSN
////////////////////////////////////////////////////////////////////////////////
package Model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class user. It contains information related to a user's state in the MSN.
 * @author Juan Luis
 */
public class User {
    /**
     * User's id.
     */
    //private int uid;
    
    /**
     * User's name.
     */
    private String name;
    
    /**
     * User's state.
     */
    private UserState state;
    
    /**
     * Time at last user's update.
     */
    private Date current_time;
            
    /**
     * Users directory.
     */
    private static final String MSNKER = "_msnsys/_kermsn/";
    
    /**
     * User's limit.
     */
    private static final int MAX_USERS = 200;
    
    /**
     * Date Format.
     */
    private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    
    /**
     * Input/Output delimiter.
     */
    private static final String IO_LIM = "\0";
    
    
 
    
    /**
     * Randomizer.
     */
    private static final Random rand = new Random();
    
    
    /**
     * Default constructor.
     * Creates a disconnected user with an invalid state, just the id is set.
     * @param uid User's id.
     */
    public User(){
        //this.uid = -1;
        this.name = "";
        this.state = UserState.OFF;
        this.current_time = null;

    }
    
    /**
     * Constructor
     * @param name User's name.
     */
    public User(String name){
        this.name = name;
        this.state = UserState.ONLINE;
        this.current_time = new Date();

    }
    
    public User(String name, UserState state, Date date){
        this.name = name;
        this.state = state;
        this.current_time = date;
    }


    
    // ---------- GETTER METHODS ---------- //
    
       
    /**
     * Get User's name.
     * @return User's name.
     */
    public String getName(){
        return name;
    }
    
    /**
     * Get User's state.
     * @return User's state.
     */
    public UserState getState(){
        return state;
    }
    
    /**
     * Get User's date.
     * @return User's date.
     */
    public Date getDate(){
        return current_time;
    }
    
    /**
     * Gets date format used in user's output
     * @return Date format.
     */
    public static DateFormat getDateFormat(){
        return df;
    }
    /**
     * Get User's directory.
     * @return User's directory.
     */
    public static String getUserDir(){
        return MSNKER;
    }
    
    /**
     * Get User's file.
     */
    public static String getUserFile(int uid){
        return MSNKER + "_usr" + Integer.toString(uid) + ".usr";
    }
    
    /**
     * 
     * @return Max number of users.
     */
    public static int getMaxUsers(){
        return MAX_USERS;
    }
    
    // ---------- PUBLIC METHODS ---------- //
        
    /**
     * Updates user.
     * @param newid If it is true, user id is also updated.
     * @throws UserOverflowException 
     */
    public void update(){
        if(state != UserState.OFF){
            current_time = new Date();
        }
    }
    
    /**
     * Updates user state.
     */
    public void changeState(UserState state){
        this.state = state;
    }
    
    /**
     * Checks if user state is valid.
     */
    public boolean validState(){
        return state != UserState.OFF;
    }
    
    /**
     * Gets a string with user's info.
     * @return String with user's info.
     */
    @Override
    public String toString(){
        return  "\nName: " + name + "\nState: "
                    + state.toString() + "\nLast update: " + df.format(current_time);
    }
    
}
