/*
 * Author: Juan Luis Su�rez D�az
 * July, 2016
 * No More Dropbox MSN
 */
package Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Class pair. A utility to mage pairs of structures.
 * @author Juan Luis
 */
class Pair<T,S>{
    /**
     * First element of the pair.
     */
    public T first;
    
    /**
     * Second element of the pair.
     */
    public S second;
    
    /**
     * Default constructor.
     */
    Pair(){
        first = null;
        second = null;
    }
    
    /**
     * Constructor.
     * @param t First element of the pair.
     * @param s Second element of the pair.
     */
    Pair(T t, S s){
        this.first = t;
        this.second = s;
    }
}

/**
 * Tracer class. A singleton class for debugging and recording.
 * @author Juan Luis
 */
public class Tracer {
    /**
     * String with debug information.
     */
    public static final String debugInfo = 
            "Debug levels:"+
            "\n\t 0 - No traces."+
            "\n\t 1 - Basic actions."+
            "\n\t 2 - Basic trace of communication messages and exceptions and details on actions."+
            "\n\t 3 - Details on exceptions."+
            "\n\t 4 - Details on communication messages."+
            "\n\t 5 - Details for everything and expansion for arrays."+
            "";
    
    /**
     * Instance of the single Tracer object.
     */
    private static Tracer instance = null;
    
    /**
     * Debug level used in printing.
     */
    private int debugLevel;
    
    /**
     * History of exceptions.
     */
    private ArrayList<Pair<Date,Exception>> errorRecord;
    
    /**
     * History of messages.
     */
    private ArrayList<Pair<Date,CSMessage>> msgRecord;
    
    /**
     * History of actions.
     */
    private ArrayList<Pair<Date,String>> actionsRecord;
    
    /**
     * Private constructor.
     */
    private Tracer(){
        this.debugLevel = 0;
        this.errorRecord = new ArrayList<>();
        this.msgRecord = new ArrayList<>();
        this.actionsRecord = new ArrayList<>();
    }
    
    /**
     * Obtains the instance for the only class object.
     * @return Tracer object.
     */
    public static Tracer getInstance(){
        if(instance == null){
            instance = new Tracer();
        }
        return instance;
    }

    /**
     * Gets debug level.
     * @return Debug level.
     */
    public int getDebugLevel() {
        return debugLevel;
    }

    /**
     * Sets debug level.
     * @param debugLevel New level to set.
     */
    public void setDebugLevel(int debugLevel) {
        this.debugLevel = debugLevel;
    }
    
    private static String getDateStringFormat(Date date){
        return "["+MSNDateFormat.getInstance().format(date)+"] ";
    }
    
    private void record(Date date, CSMessage msg){
        msgRecord.add(new Pair<>(date, msg));
        if(msgRecord.size() > 100) msgRecord.remove(0);
        switch(debugLevel){
            case 0:
            case 1:
                break;
            case 2:
            case 3:
                System.out.println(getDateStringFormat(date)+msg.getMessageCode()+" "+msg.getMessageKind()+ " received.");
                break;
            case 4:
                System.out.println(getDateStringFormat(date)+msg.getMessageCode()+" "+msg.getMessageKind()+ " received.");
                System.out.println("Date: "+getDateStringFormat(msg.getDate()));
                if(msg.getData()!=null)
                for(Object o : msg.getData()){
                    if(o != null)System.out.println(o.getClass().toString()+": "+o.toString());
                    else System.out.println("--NULL ARGUMENT--");
                }
                break;
            case 5:
                System.out.println(getDateStringFormat(date)+msg.getMessageCode()+" "+msg.getMessageKind()+ " received.");
                System.out.println("Date: "+getDateStringFormat(msg.getDate()));
                if(msg.getData() != null)
                for(Object o : msg.getData()){
                    if(o != null){
                        if(o.getClass().isArray()){
                            try{
                                System.out.println(o.getClass().toString()+": "+Arrays.deepToString((Object[])o));
                            }
                            catch(Exception ex){
                                ex.printStackTrace();
                            }
                        }
                        else System.out.println(o.getClass().toString()+": "+o.toString());
                    }
                    else System.out.println("--NULL ARGUMENT--");
                }
                else System.out.println("--NULL ARGUMENTS--");
                break;
            default:
                System.err.println("Incorrect debug level. Please choose a correct one.");
                break;
                
                
        }
    }
    
    private void record(Date date, Exception ex){
        errorRecord.add(new Pair<>(date,ex));
        if(errorRecord.size() > 100) errorRecord.remove(0);
        switch(debugLevel){
            case 0:
                break;
            case 1:
            case 2:
                System.err.println(getDateStringFormat(date)+"Error: "+ex.getMessage());
                break;
            case 3:
            case 4:
            case 5:
                System.err.println(getDateStringFormat(date)+"Error: "+ex.getMessage());
                ex.printStackTrace();
                break;
            default:
                System.err.println("Incorrect debug level. Please choose a correct one.");
                break;
        }
    }
    
    private void record(Date date, int levelFor, String action){
        actionsRecord.add(new Pair<>(date,action));
        if(actionsRecord.size() > 100) actionsRecord.remove(0);
        if(debugLevel >= levelFor){
            System.out.println(getDateStringFormat(date)+action);
        }
    }
    
    public void trace(CSMessage msg){
        record(new Date(),msg);
    }
    
    public void trace(Exception ex){
        record(new Date(),ex);
    }
    
    public void trace(int levelFor, String action){
        record(new Date(),levelFor,action);
    }
    
    
    
    
}
