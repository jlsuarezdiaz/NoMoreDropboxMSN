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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Class message. It represents the messages that users send in MSN.
 * @author Juan Luis
 */
public class Message {
        
    /**
     * Message contents.
     */
    private String messageData;
    
    /**
     * Message date.
     */
    private Date date;
    
    /**
     * Index for the message.
     */
    private int seqNumber;
    
    /**
     * User who sent the message.
     */
    private User sender;
    
    /**
     * Indicates if the message is public.
     */
    private boolean isPublic;
    
    /**
     * Indicates the message status (sending, received, doble check azul,...)
     */
    private MessageStatus status;
    
    /**
     * Private method to set message attributes.
     * @param text Message text.
     * @param sender User who sends the message.
     * @param seqNumber Message index.
     * @param isPublic Indicates if the message is public or private.
     */
    private void set(String text,User sender, int seqNumber, boolean isPublic){
        this.messageData = text;
        this.date = new Date();
        this.sender = sender;
        this.seqNumber = seqNumber;
        this.isPublic = isPublic;
        this.status = MessageStatus.UNDEFINED;
    }
    
    /**
     * Default constructor.
     */
    public Message(){
        set("",null,-1,true);
    }
    
    /**
     * Constructor.
     * @param text Message text.
     * @param sender User who sends the message.
     * @param seqNumber Message index.
     * @param isPublic Indicates if the message is public or private.
     */
    public Message(String text,User sender, int seqNumber, boolean isPublic){
        set(text, sender, seqNumber, isPublic);
    }
    
    /**
     * Gets the text of the message.
     * @return Message text.
     */
    public String getText(){
        return messageData;
    }
    
    /**
     * Gets the date of the message.
     * @return Message date. 
     */
    public Date getDate(){
        return date;
    }

    /**
     * Gets the message number.
     * @return Message number.
     */
    public int getSeqNumber() {
        return seqNumber;
    }

    /**
     * Gets the message sender.
     * @return Sender.
     */
    public User getSender() {
        return sender;
    }
    
    
    

    /**
     * Gets the message status.
     * @return Message status.
     */
    public MessageStatus getStatus() {
        return status;
    }

    /**
     * Indicates if message is public or private.
     * @return True, if and only if message is public.
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Sets the message status.
     * @param status Status to set.
     */
    public void setStatus(MessageStatus status) {
        this.status = status;
    }
    
    /**
     * Adds a header to the message text.
     * @param header Text to add as header.
     */
    public void addHeader(String header){
        this.messageData=header+this.messageData;
    }
    
    /**
     * Indicates whether message content has any text.
     * @return true, when message text is empty. Else, false.
     */
    public boolean isEmpty(){
        return messageData.trim().isEmpty();
    }
}
