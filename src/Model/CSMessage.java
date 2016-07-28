/*
 * Author: Juan Luis Suárez Díaz
 * July, 2016
 * No More Dropbox MSN
 */
package Model;

import java.util.Date;

/**
 * A class to manage and encapsulate communication messages between server and clients.
 * @author Juan Luis
 */
public class CSMessage {
    
    /**
     * Message kind.
     */
    private MessageKind messageKind;
    
    /**
     * Message date.
     */
    private Date date;
    
    /**
     * Message arguments.
     */
    private Object[] args;
    
    /**
     * Constructor.
     * @param kind Message kind.
     * @param args Message arguments.
     */
    CSMessage(MessageKind kind, Object[] args){
        this.messageKind = kind;
        this.date=new Date();
        this.args = args;
    }
    
    /**
     * Constructor.
     * @param msgCode Message code.
     * @param args Message arguments.
     */
    CSMessage(int msgCode, Object[] args){
        this.messageKind=MessageKind.valueOf(msgCode);
        this.date = new Date();
        this.args = args;
    }
    
    /**
     * @deprecated 
     * @return 
     */
    public byte[] toByteArray(){
        throw new UnsupportedOperationException("Not supported yet (maybe never will be)");
    }
    
    /**
     * @deprecated 
     * @param data
     * @return 
     */
    public static CSMessage buildMessage(byte[] data){
        throw new UnsupportedOperationException("Not supported yet (maybe never will be)");
    }
    
    public MessageKind getMessageKind(){
        return messageKind;
    }
    
    public Date getDate(){
        return date;
    }
    
    public Object[] getData(){
        return args;
    }
    
    public Object getData(int i){
        return args[i];
    }
    
    /** 
     * DATA FOR EACH KIND OF MESSAGE ([?] = optional)
     * 
     * HELO:
     * 
     * LOGIN: name (String)
     * 
     * BYE:
     * 
     * LOGOUT:
     * 
     * DISC:
     * 
     * KILL: errorMsg (String)[?]
     * 
     * UPDATE_DOWNLOAD:
     * 
     * OK: anything
     * 
     * OK_SEND: userNum (int)?, msgNum (int)
     * 
     * OK_PRIV: isPriv (boolean)
     * 
     * OK_SLCT: userNum (int), isSelected (boolean)
     * 
     * OK_STATE: state (UserState)
     * 
     * OK_FILE: 
     * 
     * OK_VERSION:
     * 
     * USERS: userList (User[])
     * 
     * SEND: senderNum (int), msgNum (int), text (String)
     * 
     * FILE:
     * 
     * IMALIVE:
     * 
     * CHANGE_PRIV: mode (boolean)
     * 
     * CHANGE_SLCT: user (int), mode (boolean)
     * 
     * CHANGE_STATE: state (userState)
     * 
     * ERR_*: msg (String) [?]
     */
}
