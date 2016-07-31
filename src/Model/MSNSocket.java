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
    public synchronized void writeMessage(CSMessage msg) throws IOException{
        this.oos.writeObject(msg);
        this.oos.flush();
    }
    
    /**
     * Reads a message from the socket.
     * @return CSMessage received in the socket.
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public synchronized CSMessage readMessage() throws IOException, ClassNotFoundException{
        return (CSMessage)this.ois.readObject();
    }
    
    /**
     * Closes the socket.
     */
    public void close() throws IOException{
        socket.close();
    }
}
