/*
 * Author: Juan Luis Suárez Díaz
 * July, 2015
 * Music Player
 */
package Model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author Juan Luis
 */
public interface Communicator {
    public InputStream getInputStream() throws IOException;
    
    public OutputStream getOutputStream() throws IOException;
    
    public OutputStreamWriter getOutputStreamWriter();
    
    public Scanner getInputScanner();
    
    public Socket getSocket();
}
