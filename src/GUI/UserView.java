////////////////////////////////////////////////////////////////////////////////
// Author: Juan Luis Suarez Diaz
// Jun, 2015
// Dropbox MSN
////////////////////////////////////////////////////////////////////////////////
package GUI;

import Model.User;
import Model.UserState;
import java.awt.Color;
import java.awt.Font;

/**
 * Class UserView.
 * A GUI for class User.
 * @author Juan Luis
 */
public class UserView extends javax.swing.JPanel {
    /**
     * User.
     */
    private User userModel;
    
    /**
     * Selected checker.
     */
    private boolean isSelected;
    
    /**
     * User id.
     */
    private int id;
    
    /**
     * Private method to set colors.
     * @param c Color to set.
     */
    private void setTextColors(Color c){
        this.labelName.setForeground(c);
        this.labelState.setForeground(c);
        this.labelDate.setForeground(c);
    }
    
    /**
     * Private method to fit the font size according to name length.
     */
    private void setNameFont(){
        // Compute the font size needed for the user name.
        String name = userModel.getName();
        Font labelFont = labelName.getFont();
        
        int stringWidth = labelName.getFontMetrics(labelFont).stringWidth(name);
        double componentWidth = 120;

        // If the name is larger than the label width then the font size is reduced.
        if (stringWidth > componentWidth){
            // Find out how much the font can grow in width.
            double widthRatio = componentWidth / (double) stringWidth;
            int newFontSize = (int)(labelFont.getSize() * widthRatio);
            // Set the label's font size to the newly determined size.
            labelName.setFont(new Font(labelFont.getName(), Font.BOLD , newFontSize));
        }
    }
    
    /**
     * Private method to set background according to selection.
     */
    private void setBackground(){
        if(isSelected){
            this.setBackground(new Color(0x00FFFF));
        }
        else{
            this.setBackground(new Color(0x999999));
        }
        /**
         * 0x00FFFF cian
         * 0xF0F0F0 gris claro
         * 0x999999 gris oscuro
         */
    }
    
    /**
     * Creates new form UserView
     */
    public UserView() {
        initComponents();
        isSelected = false;
    }
    
    /**
     * Get the user
     * @return Model User.
     */
    public User getUser(){
        return userModel;
    }
    
    public int getUserId(){
        return id;
    }
    
    /**
     * @return true if and only if the user is selected.
     */
    public boolean isSelected(){
        return isSelected;
    }
    
    /**
     * Selects or unselects the view.
     * @param selection Boolean indicating selection or not.
     */
    public void select(boolean selection){
        isSelected = selection;
        setBackground();
        repaint();
    }
    
    /**
     * Set the view with a user.
     * @param u User.
     */
    public void setUser(User u, int id){
        isSelected = false;
        this.userModel = u;
        this.id = id;
        
        setNameFont();
        this.labelName.setText(u.getName());
        
        this.labelState.setText(u.getState().toString());
        this.labelDate.setText((u.getDate() == null)?"":User.getDateFormat().format(u.getDate()));
        if(userModel.getState() == UserState.OFF){
            this.setVisible(false);
        }
        else{
            this.setVisible(true);
            if(userModel.getState() == UserState.ONLINE)
                this.setTextColors(Color.GREEN);
            else if(userModel.getState() == UserState.ABSENT)
                this.setTextColors(Color.ORANGE);
            else if (userModel.getState() == UserState.BUSY)
                this.setTextColors(Color.RED);
        }
        repaint();
    }

    /**
     * Set visible method.
     * @param bln 
     */
    @Override
    public void setVisible(boolean bln){
        super.setVisible(bln);
        isSelected = false;
        setBackground();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelName = new javax.swing.JLabel();
        labelState = new javax.swing.JLabel();
        labelDate = new javax.swing.JLabel();

        setBackground(new java.awt.Color(153, 153, 153));
        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setMaximumSize(new java.awt.Dimension(139, 89));
        setMinimumSize(new java.awt.Dimension(139, 89));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });

        labelName.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        labelName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelName.setText("Usuario");

        labelState.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        labelState.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelState.setText("ONLINE");

        labelDate.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        labelDate.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelDate.setText("31/07/1995 13:30:00");
        labelDate.setToolTipText("Última actualización (Last update).");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelState, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(labelDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(labelName, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelName, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelState, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelDate, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Mouse clicked event. Allows selection.
     * @param evt 
     */
    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        select(!isSelected);
    }//GEN-LAST:event_formMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel labelDate;
    private javax.swing.JLabel labelName;
    private javax.swing.JLabel labelState;
    // End of variables declaration//GEN-END:variables
}
