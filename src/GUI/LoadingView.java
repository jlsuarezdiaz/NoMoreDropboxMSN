/*
 * Author: Juan Luis Suárez Díaz
 * July, 2015
 * Music Player
 */
package GUI;

/**
 *
 * @author Juan Luis
 */
public class LoadingView extends javax.swing.JDialog implements LoadableView {
    private String fileLoading;
    private int curr;
    private int tot;
    private String unit;
    private String  action;
    
    /**
     * Creates new form LoadingView
     */
    public LoadingView(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }
    
    @Override
    public void showView(){
        this.setVisible(true);
    }
    
    @Override
    public void hideView(){
        this.dispose();;
    }
    
    @Override
    public void setView(String fileLoading, int curr, int tot, String unit, String action){
        this.fileLoading = fileLoading;
        this.curr = curr;
        this.tot = tot;
        this.unit = unit;
        this.action = action;
        this.loadingLab.setText(action + " " + fileLoading + "...");
        this.quantityLab.setText(Integer.toString(curr) + " / " + Integer.toString(tot) + " " + unit);
        this.loadPb.setMaximum(tot);
        this.loadPb.setMinimum(0);
        this.loadPb.setValue(curr);
        this.repaint();
        this.revalidate();
    }
    
    @Override
    public void updateView(int curr, int tot) {
        setView(this.fileLoading,curr,tot,this.unit,this.action);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        loadPb = new javax.swing.JProgressBar();
        loadingLab = new javax.swing.JLabel();
        quantityLab = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Loading...");
        setAlwaysOnTop(true);

        loadingLab.setFont(new java.awt.Font("Calibri", 1, 12)); // NOI18N
        loadingLab.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        loadingLab.setText("Cargando archivo: NoMoreDropboxMSN.jar...");

        quantityLab.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        quantityLab.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        quantityLab.setText("123560 / 10256000 B  ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(loadPb, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(loadingLab, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(quantityLab, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(loadingLab, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(loadPb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(quantityLab)
                .addContainerGap(76, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LoadingView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LoadingView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LoadingView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoadingView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                LoadingView dialog = new LoadingView(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar loadPb;
    private javax.swing.JLabel loadingLab;
    private javax.swing.JLabel quantityLab;
    // End of variables declaration//GEN-END:variables

    
}
