/*
 * Author: Juan Luis Suárez Díaz
 * July, 2016
 * No More Dropbox MSN
 */
package Model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A singleton class to manage everywhere the date formats. 
 * @author Juan Luis
 */
public class MSNDateFormat {
    /**
     * Date Format.
     */
    private final DateFormat df;
    
    /**
     * Class instance.
     */
    private static MSNDateFormat instance = null;
    
    /**
     * Private constructor.
     */
    private MSNDateFormat(){
        df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");    
    }
    
    /**
     * Method to access to the single object of the class.
     * @return The only MSNDateFormat instance.
     */
    public static MSNDateFormat getInstance(){
        if(instance == null) instance = new MSNDateFormat();
        return instance;
    }
    
    /**
     * Converts date to string.
     * @param d Date to convert.
     * @return String showing the date. The format is: "dd/MM/yyyy HH:mm:ss"
     */
    public String format(Date d){
        return df.format(d);
    }
    
    /**
     * COnverts string to date.
     * @param s String to convert.
     * @pre The string must follow the format: "dd/MM/yyyy HH:mm:ss"
     * @return Date object with the date given in the string.
     * @throws ParseException 
     */
    public Date parse(String s) throws ParseException{
        return df.parse(s);
    }
    
}
