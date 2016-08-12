/*
 * Author: Juan Luis Suárez Díaz
 * July, 2016
 * No More Dropbox MSN
 */
package Model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;

/**
 * A class to manage app sockets.
 * @author Juan Luis
 */
public class MSNSocket {
    /**
     * Java internal socket.
     */
    private Socket socket;
    
    /**
     * Object output stream.
     */
    private ObjectOutputStream oos;
    
    /**
     * ObjectInputStream.
     */
    private ObjectInputStream ois;
    
    /**
     * Input mutex.
     */
    private final Object inMutex = new Object();
    
    /**
     * Output mutex.
     */
    private final Object outMutex = new Object();
    
    /**
     * Constructor.
     * @param host Server host.
     * @param port Server port.
     * @throws IOException 
     */
    public MSNSocket(String host, int port) throws IOException{
        this.socket = new Socket(host, port);
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());
    }
    
    /**
     * Constructor
     * @param socket Java socket. 
     */
    public MSNSocket(Socket socket) throws IOException{
        this.socket = socket;
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());
    }
    
    /**
     * Sends a message over the socket.
     * @param msg CSMessage to send.
     * @throws IOException
     */
    public void writeMessage(CSMessage msg) throws IOException{
        synchronized(outMutex){
            //System.out.println("["+new Date() + "] Sending "+msg.getMessageKind()+"...");
            this.oos.reset();
            this.oos.writeObject(msg);
            //this.oos.writeUnshared(msg);
            this.oos.flush();
            //System.out.println("["+new Date() + "] Sent "+msg.getMessageKind()+"...");
        }
    }
    
    /**
     * Reads a message from the socket.
     * @return CSMessage received in the socket.
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public CSMessage readMessage() throws IOException, ClassNotFoundException{
        CSMessage read;
        synchronized(inMutex){
            read =  (CSMessage)this.ois.readObject();
            //read =  (CSMessage)this.ois.readUnshared();
        }
        return read;
    }
    
    /**
     * Closes the socket.
     */
    public void close() throws IOException{
        socket.close();
    }
    
    /**
     * Checks if the socket is closed.
     * @return True, when socket has been closed.
     */
    public boolean isClosed(){
        return socket.isClosed();
    }
    
    /**
     * Checks if the connection with remote host is still alive.
     * This method tries to send three times a NOP message.
     * If any of the tries is succesful, the connection will be alive.
     * If every try fails, the connection will be closed.
     * @return True, if the connection is alive. Else, false.
     */
    public boolean isConnectionAlive(){
        Tracer.getInstance().trace(2, "Connection checking started.");
        boolean isAlive = false;
        CSMessage tryMsg = new CSMessage(MessageKind.NOP, null);
        final int numTries = 3;
        
        for(int i = 0; i < numTries && !isAlive; i++){
            try{
                writeMessage(tryMsg);
                isAlive = true;
                Tracer.getInstance().trace(2,"Attempt "+Integer.toString(i)+" succeded.");
            }
            catch(Exception ex){
                Tracer.getInstance().trace(ex);
                Tracer.getInstance().trace(2,"Attempt "+Integer.toString(i)+" failed.");
            }
        }
        if(!isAlive){
            try{
                socket.close();
            }
            catch(Exception ex){}
        }
        return isAlive;
    }
}
