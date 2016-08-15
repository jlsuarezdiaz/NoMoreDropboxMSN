/*
 * Author: Juan Luis Su�rez D�az
 * July, 2016
 * No More Dropbox MSN
 */
package GUI;

import Model.Message;
import javax.swing.JPanel;

/**
 * MessageableView interface.
 * This interface defines the functions that every Message structure
 * must have in the Messenger GUI.
 * @author Juan Luis
 */
public abstract class MessageableView extends JPanel{
    /**
     * @return true if and only if the user is selected.
     */
    public abstract boolean isSelected();
    
    /**
     * Selects or unselects the view.
     * @param selection Boolean indicating selection or not.
     */
    public abstract void select(boolean selection);
    
    /**
     * Set the view for a message.
     * @param m Message to set.
     */
    public abstract void setMessage(Message m);
    
    /**
     * Gets the message referenced by the view.
     * @return message.
     */
    public abstract Message getMessage();
}
