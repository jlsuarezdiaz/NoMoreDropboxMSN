/*
 * Author: Juan Luis Su�rez D�az
 * July, 2016
 * No More Dropbox MSN
 */
package Model;

import GUI.FileView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class FileStruct. A class with all the data required for file management.
 * @author Juan Luis
 */
class FileStruct{
    /**
     * File.
     */
    private File f;
    
    /**
     * File length.
     */
    private long totalLength;
    
    /**
     * File length currently read.
     */
    private long currentLength;
    
    /**
     * File view associated to the file.
     */
    private FileView fileView;
    
    /**
     * File's output stream.
     */
    private FileOutputStream fileOutputStream;
    
    /**
     * Constructor. Creates a temporary file. To save file once loaded, use saveFile method.
     * @param fileName File name.
     * @param totalSize File size.
     */
    public FileStruct(String fileName, long totalSize, FileView fileView){
        try{
            //// TMP FILE
            String [] ss = fileName.split("\\.");
            String suf = ss[ss.length -1];
            String pref = "";
            for(int i = 0; i < ss.length-1; i++) pref += ss[i];
            if(pref.length() < 3) pref = "___"+pref;

            f = File.createTempFile(pref, suf);
            fileOutputStream = new FileOutputStream(f);
            
            ////
            this.totalLength = totalSize;
            this.currentLength = 0;
            this.fileView = fileView;
            this.fileView.setView(fileName,0,totalSize,"B", "Descargando...");
        }
        catch(Exception ex){
            Tracer.getInstance().trace(ex);
        }
    }
    
    public void writeData(byte [] data, long iniByte, int offset){
        try{
            if(iniByte != currentLength){
                throw new Exception("Invalid file sequence: "+Long.toString(currentLength)+ " vs "+Long.toString(iniByte));
            }
            fileOutputStream.write(data,0,offset);
            this.currentLength+=offset;
            
            if(fileView != null){
                //Set view.
                fileView.updateView(currentLength, totalLength);
                if(isFullFile()) fileView.setFile(f);
            }
        }
        catch(Exception ex){
            Tracer.getInstance().trace(ex);
        }
    }
    
    public boolean isFullFile(){
        return currentLength==totalLength;
    }
    
    public void saveFile(Path newPath){
        try {
            Files.move(f.toPath(), newPath, REPLACE_EXISTING);
        } catch (IOException ex) {
            Tracer.getInstance().trace(ex);
        }
    }
    
}

/**
 * Class FileRegistry. A class for files management.
 * @author Juan Luis
 */
public class FileRegistry {
    /**
     * Files registry.
     */
    Map<Integer,FileStruct> [] registry;
    
    public FileRegistry(){
        this.registry = (Map<Integer, FileStruct>[]) new ArrayList<>(User.getMaxUsers()).toArray();
        for(Map<Integer,FileStruct> m : registry){
            m = new ConcurrentHashMap<>();
        }
    }
    
    public void addNewFile(int userId, int fileId, String name, long length, FileView view){
        registry[userId].put(fileId,new FileStruct(name,length , view));
    }
    
    public void addData(int userId, int fileId, byte[] data, long iniByte, int offset){
        registry[userId].get(fileId).writeData(data, iniByte, offset);
    }
}
