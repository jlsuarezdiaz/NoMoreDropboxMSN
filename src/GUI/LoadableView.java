/*
 * Author: Juan Luis Suárez Díaz
 * July, 2015
 * NoMoreDropboxMSN
 */
package GUI;

/**
 * Interface for any view which implements a loading-GUI.
 * @author Juan Luis
 */
public interface LoadableView {
    public void showView();
    
    public void hideView();
    
    public void setView(String fileLoading, long curr, long tot, String unit, String action);

    public void updateView(long curr, long tot);
}
