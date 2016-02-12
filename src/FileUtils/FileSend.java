/*
 * Author: Juan Luis Suárez Díaz
 * July, 2015
 * Music Player
 */
package FileUtils;

import GUI.LoadableView;
import GUI.LoadingView;
import Model.Communicator;
import Model.Message;
import Model.MessageKind;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class with functions to send and receive files.
 * @author Juan Luis
 */
public class FileSend {
    public static void sendFile(Communicator c, byte[] data, String name, LoadableView view){
        Socket s = c.getSocket();
        String msgcab = new Message(MessageKind.FILE,new String[]{name,Integer.toString(data.length)}).toMessage();
        OutputStream os = null;
        OutputStreamWriter o = null;
        
        try{
            os = c.getOutputStream();
            o = c.getOutputStreamWriter();

            System.out.println("Se enviarán "+data.length+" B a [USER]");
            o.write(msgcab);
            o.flush();
            os.write(data, 0, data.length);
            //o.write(new String(data,StandardCharsets.UTF_8));
            //outputStreams[id].write("\n\nENDFILE\n\n");
            o.flush();
            System.out.println(data.length + " B enviados a [USER]");

        }
        catch(Exception ex){
            System.err.println("Error: "+ex.getMessage());
        }
        finally{
            try{
            //if(os != null) o.close();
            //if(o != null) o.close();
            }catch(Exception ex){}
        }
    }
    
    public static byte[] receiveFile(Communicator c, int size, LoadableView view){
        Socket s = c.getSocket();
        try{
            byte[] read = new byte[size];
            int rec = 0;
            int totalRec = 0;
            InputStream input = c.getInputStream();
            
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
    
    //!!!!!!!!!!!!!!!!!!!!!!!// :(
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
                if(rec >= 0) totalRec += rec;
                
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
    
    public static byte[] loadFile(String address) throws IOException{
        File f = new File(address);
        byte[] data = Files.readAllBytes(f.toPath());
        return data;
    }

    public static void sendFileProtocol(File f, Communicator c) throws IOException{    
        byte[] data = FileUtils.FileSend.loadFile(f.getAbsolutePath());
        FileUtils.FileSend.sendFile(c, data,f.getName(), null);    
    }
    
    public static File receiveFileProtocol(Communicator c,String name,int size, LoadableView v){
        File ret = null;             
        FileOutputStream fileOutputStream = null;

        // v.setView("./NoMoreDropboxMSN.jar", 0, 0, "B", "Descargando archivo:");
        byte[] data = receiveFile(c,size,v);

        try {
           String [] ss = name.split("\\.");
           String suf = ss[ss.length -1];
           String pref = "";
           for(int i = 0; i < ss.length-1; i++) pref += ss[i];
           if(pref.length() < 3) pref = "___"+pref;
           
           
           ret = File.createTempFile(pref, suf);
           fileOutputStream = new FileOutputStream(ret); 
           fileOutputStream.write(data);
           
        }
        catch(Exception ex){
            System.err.println("Error: " + ex.getMessage());
            ret = null;
        }
        finally {
           if(fileOutputStream != null) try {
               fileOutputStream.close();
           } catch (IOException ex) {}
        }

        return ret;
    }
}
