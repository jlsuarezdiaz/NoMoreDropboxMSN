/*
 * Author: Juan Luis Suárez Díaz
 * July, 2015
 * Music Player
 */
package FileUtils;

import GUI.LoadableView;
import java.io.InputStream;
import java.net.Socket;

/**
 * A class with functions to send and receive files.
 * @author Juan Luis
 */
public class FileSend {
    public static byte[] receiveFile(Socket s, int size, LoadableView view){
        try{
            byte[] read = new byte[size];
            int rec = 0;
            int totalRec = 0;
            InputStream input = s.getInputStream();
            
            if(view!=null){
                view.updateView(totalRec, size);
                view.showView();
            }
            
            do{
                rec = input.read(read,totalRec,size-totalRec);
                if(rec != -1) totalRec += rec;
                System.out.println(totalRec + " B recibidos.");
                if(view != null) view.updateView(totalRec, size);
            }while(totalRec < size);
            if(view!=null) view.hideView();
            return read;
        }
        catch(Exception ex){
            System.out.println("Error: "+ex.getMessage());
            return null;
        }
    }
}
