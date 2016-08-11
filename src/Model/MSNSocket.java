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
    
    public boolean isClosed(){
        return socket.isClosed();
    }
}
