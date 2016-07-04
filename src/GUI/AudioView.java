/*
 * Author: Juan Luis Suárez Díaz
 * July, 2015
 * Music Player
 */
package GUI;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import static java.lang.Thread.sleep;
import java.nio.file.Paths;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

enum MusicPlayerState{
    PLAYING,PAUSED,STOPPED
}
class MusicPlayer{
    /**
     * JFX Panel
     */
    private static JFXPanel jfx = new JFXPanel();
    
    /**
     * Internal media player.
     */
    private MediaPlayer player;
    
    private Media song;
    
    
    
    //------------------//
    private MusicPlayerState state;
    
    private double length;
    
    private String songName;
    
    
    
    //------------------//
    
    MusicPlayer(File f){
        this.song = new Media(Paths.get(f.getAbsolutePath()).toUri().toString());
        this.player = new MediaPlayer(song);
        while(player.getStatus() != MediaPlayer.Status.READY){
            try {
                sleep(10);
            } catch (InterruptedException ex) {}
        }
        this.length = player.getTotalDuration().toSeconds();
        this.state = MusicPlayerState.STOPPED;
        this.songName = f.getName();
        this.player.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                player.stop();
                state = MusicPlayerState.STOPPED;
            }
        });
    }
    
    public void play(){
        player.play();
        this.state = MusicPlayerState.PLAYING;
    }
    
    public void pause(){
        player.pause();
        this.state = MusicPlayerState.PAUSED;
    }
    
    public void stop(){
        player.stop();
        this.state = MusicPlayerState.STOPPED;
    }
    
    public void backward(){
        moveSong(-1000);
    }
    
    public void forward(){
        moveSong(1000);
    }
    
    /**
     * Moves the song the time specified by \e time.
     * @param time Time to move (in ms). A negative value means rewind. 
     */
    public void moveSong(double time){
        player.seek(Duration.millis(this.getElapsedTime()*1000+time));
    }
    
    public MusicPlayerState getState(){
        return state;
    }
    
    public double getLength(){
        return length;
    }
    
    /**
     * Gets song's elapsed time.
     * @return Elapsed time (in seconds). 
     */
    public double getElapsedTime(){
        return (player==null)?0:player.currentTimeProperty().get().toSeconds();
    }
    
    public String getSongName(){
        return songName;
    }
    
    public void seek(Duration time){
        player.seek(time);
    }
}



/**
 *
 * @author Juan Luis
 */
public class AudioView extends javax.swing.JDialog {
    
    private MusicPlayer musicPlayer;
    
    private Timer updater;
    
    private static final int updaterPeriod = 10;
    
    private AudioView myInstance;
    
    private double seekQty;
    
    /**
     * Date format for songs time.
     */
    private static final DateFormat df = new SimpleDateFormat("HH:mm:ss");
    static {df.setTimeZone(TimeZone.getTimeZone("GMT"));}

    /**
     * Creates new form AudioView
     */
    public AudioView(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.myInstance = this;
        this.musicPlayer = null;
        this.seekQty=0;

        this.updater = new Timer(updaterPeriod, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if(musicPlayer.getState()!=MusicPlayerState.PLAYING){
                    myInstance.BtPlayPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Media/play_button.png")));
                    myInstance.BtPlayPause.setToolTipText("Reproducir");
                }
                else{
                    myInstance.BtPlayPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Media/pause_button.png")));
                    myInstance.BtPlayPause.setToolTipText("Pausa");
                }
                
                if(seekQty!=0){
                    musicPlayer.moveSong(seekQty);
                }

                    double cur_ms = musicPlayer.getElapsedTime() * 1000;
                    double tot_ms = musicPlayer.getLength()*1000;
                    myInstance.timeScroll.setMaximum((int) (tot_ms));
                    myInstance.timeScroll.setValue((int) (cur_ms));
                    Time cur = new Time((long) cur_ms);
                    Time tot = new Time((long) tot_ms);

                    myInstance.timeLab.setText(df.format(cur) + " / " + df.format(tot) + " ");
                
                myInstance.repaint();
                myInstance.revalidate();
                
            }
            
        });
        this.addWindowListener (new WindowAdapter() {
            @Override
            public void windowClosing (WindowEvent e) {
                musicPlayer.stop();
                dispose();
            }
        });
    }
    
    public void setView(File f){
        this.musicPlayer = new MusicPlayer(f);
        myInstance.titleLab.setText(musicPlayer.getSongName());
        //myInstance.timeScroll.setMaximum((int) musicPlayer.getLength());
        myInstance.timeScroll.setMinimum(0);
        this.setVisible(true);
        this.updater.start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSlider1 = new javax.swing.JSlider();
        BtBackward = new javax.swing.JButton();
        BtPlayPause = new javax.swing.JButton();
        BtForward = new javax.swing.JButton();
        BtStop = new javax.swing.JButton();
        titleLab = new javax.swing.JLabel();
        timeScroll = new javax.swing.JSlider();
        timeLab = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/Media/msn_icon.png")));
        setResizable(false);

        BtBackward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Media/backward_button.png"))); // NOI18N
        BtBackward.setToolTipText("Rebobinar");
        BtBackward.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                BtBackwardMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                BtBackwardMouseReleased(evt);
            }
        });
        BtBackward.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtBackwardActionPerformed(evt);
            }
        });

        BtPlayPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Media/play_button.png"))); // NOI18N
        BtPlayPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtPlayPauseActionPerformed(evt);
            }
        });

        BtForward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Media/forward_button.png"))); // NOI18N
        BtForward.setToolTipText("Avanzar");
        BtForward.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                BtForwardMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                BtForwardMouseReleased(evt);
            }
        });
        BtForward.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtForwardActionPerformed(evt);
            }
        });

        BtStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Media/stop_button.png"))); // NOI18N
        BtStop.setToolTipText("Parar");
        BtStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtStopActionPerformed(evt);
            }
        });

        titleLab.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        titleLab.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titleLab.setText("sound.mp3");

        timeScroll.setValue(0);
        timeScroll.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                timeScrollStateChanged(evt);
            }
        });
        timeScroll.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                timeScrollMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                timeScrollMouseReleased(evt);
            }
        });

        timeLab.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        timeLab.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        timeLab.setText("0:00 / 12:57");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(timeScroll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(titleLab, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(BtBackward, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(BtPlayPause, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(BtForward, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(timeLab, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(BtStop, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titleLab)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(BtPlayPause, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BtBackward, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BtForward, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BtStop, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeLab))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                .addComponent(timeScroll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void BtBackwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtBackwardActionPerformed
        //musicPlayer.backward();
    }//GEN-LAST:event_BtBackwardActionPerformed

    private void BtPlayPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtPlayPauseActionPerformed
        if(musicPlayer.getState() == MusicPlayerState.PLAYING){
            musicPlayer.pause();
        }
        else{
            try{
                musicPlayer.play();
            }
            catch(MediaException ex){
                JOptionPane.showMessageDialog(this,"Error al reproducir el archivo: "+ex.getMessage(),
                    "Error de reproducción", JOptionPane.ERROR_MESSAGE);
                //songModel.endSong();
                //setStoppedView();
                //setError();
            }
        }
    }//GEN-LAST:event_BtPlayPauseActionPerformed

    private void BtForwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtForwardActionPerformed

        //musicPlayer.forward();
    }//GEN-LAST:event_BtForwardActionPerformed

    private void BtStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtStopActionPerformed
        musicPlayer.stop();
    }//GEN-LAST:event_BtStopActionPerformed

    private void timeScrollStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_timeScrollStateChanged
        if(Math.abs(timeScroll.getValue()-musicPlayer.getElapsedTime()*1000)>=1000){
            Duration time = Duration.millis(timeScroll.getValue());
            musicPlayer.seek(time);
        }
    }//GEN-LAST:event_timeScrollStateChanged

    private void timeScrollMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_timeScrollMouseReleased

    }//GEN-LAST:event_timeScrollMouseReleased

    private void timeScrollMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_timeScrollMousePressed
       
    }//GEN-LAST:event_timeScrollMousePressed

    private void BtBackwardMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BtBackwardMousePressed
        this.seekQty=-2000;
    }//GEN-LAST:event_BtBackwardMousePressed

    private void BtBackwardMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BtBackwardMouseReleased
        this.seekQty=0;
    }//GEN-LAST:event_BtBackwardMouseReleased

    private void BtForwardMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BtForwardMousePressed
        this.seekQty=2000;
    }//GEN-LAST:event_BtForwardMousePressed

    private void BtForwardMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BtForwardMouseReleased
        this.seekQty=0;
    }//GEN-LAST:event_BtForwardMouseReleased

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
            java.util.logging.Logger.getLogger(AudioView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AudioView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AudioView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AudioView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AudioView dialog = new AudioView(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton BtBackward;
    private javax.swing.JButton BtForward;
    private javax.swing.JButton BtPlayPause;
    private javax.swing.JButton BtStop;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JLabel timeLab;
    private javax.swing.JSlider timeScroll;
    private javax.swing.JLabel titleLab;
    // End of variables declaration//GEN-END:variables
}
