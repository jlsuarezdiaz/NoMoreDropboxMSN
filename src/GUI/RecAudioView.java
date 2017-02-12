/*
 * Author: Juan Luis Su�rez D�az
 * July, 2016
 * No More Dropbox MSN
 */
package GUI;

import Model.ClientController;
import Model.Tracer;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import static java.lang.Thread.sleep;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;
import javax.swing.Timer;

enum RecordState{
    START, RECORDING, RECORDED
}

/**
 *
 * @author Juan Luis
 */
public class RecAudioView extends javax.swing.JDialog {

    private Timer recAnim;
    
    private volatile RecordState recState;
    
    File recordedFile;
    
    TargetDataLine line;
    
    private int recordTime;
    
    private static final DateFormat df = new SimpleDateFormat("HH:mm:ss");
    static {df.setTimeZone(TimeZone.getTimeZone("GMT"));}
    
    private ClientController ctrl;
    
    /**
     * Creates new form RecAudioView
     */
    public RecAudioView(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        this.addWindowListener (new WindowAdapter() {
            @Override
            public void windowClosing (WindowEvent e) {
                audioPanel.stop();
                dispose();
            }
        });
        
        audioPanel.setVisible(false);
        lblRec.setVisible(false);
        recState = RecordState.START;
        line = null;
        lblTime.setText(df.format(0));
        btSend.setVisible(false);
        this.ctrl = null;
        
        recAnim = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                lblRec.setVisible(!lblRec.isVisible());
                recordTime+=1000;
                lblTime.setText(df.format(recordTime));
            }
        });
        
        try{
            recordedFile = File.createTempFile("audio", ".wav");
        }
        catch(Exception ex){
            Tracer.getInstance().trace(ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al grabar audio", JOptionPane.ERROR_MESSAGE);
            this.dispose();
        }
        
        KeyListener kl = new KeyListener() {

            @Override
            public void keyPressed(KeyEvent ke) {}

            @Override
            public void keyReleased(KeyEvent ke) {
                switch(ke.getKeyCode()){
                    case KeyEvent.VK_SPACE:
                        performRec();
                        break;
                    case KeyEvent.VK_ENTER:
                        performRec();
                        if(recState==RecordState.RECORDED) performSend();
                        break;
                }
            }

            @Override
            public void keyTyped(KeyEvent ke) {}
        };
        
        this.addKeyListener(kl);
        focus();
    }
    
    private void focus(){
        this.setFocusable(true);
        requestFocusInWindow();
    }
    
    private void performRec(){
        switch(recState){
            case START:
                performStartRec();
                break;
            case RECORDING:
                performStopRec();
                break;
            case RECORDED:
                performStartRec();
                break;
        }
        focus();
    }
    
    private void performStartRec(){
        btRec.setSelected(true);
        btSend.setVisible(false);
        this.recState = RecordState.RECORDING;
        this.recAnim.start();
        recordTime = 0;
        audioPanel.stop();
        audioPanel.setVisible(false);
        try{
            recordedFile = File.createTempFile("audio", ".wav");
        }
        catch(Exception ex){
            Tracer.getInstance().trace(ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al grabar audio", JOptionPane.ERROR_MESSAGE);
            this.dispose();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                record();
            }
        }).start();
    }
    
    private void performStopRec(){
        btRec.setSelected(false);
        
        this.recAnim.stop();
        this.lblRec.setVisible(false);

        this.recState = RecordState.RECORDED;
        line.stop();
        line.close();
        audioPanel.setView(recordedFile);
        audioPanel.setVisible(true);
        
        btSend.setVisible(true);
    }
    
    private AudioFormat getAudioFormat(){
        float sampleRate = 16000;
        int sampleSizeInBits = 8;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                                             channels, signed, bigEndian);
        return format;   
    }
    
    private void record(){
        
        AudioFormat format = getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, 
                format); // format is an AudioFormat object
        if (!AudioSystem.isLineSupported(info)) {
            JOptionPane.showMessageDialog(this, "Este dispositivo no soporta la grabación de sonido.", "Error al grabar audio", JOptionPane.ERROR_MESSAGE);
            this.dispose();
        }
        // Obtain and open the line.
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
        
            AudioInputStream ais = new AudioInputStream(line);
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, recordedFile);

        /*
            int numBytesRead;
            byte[] data = new byte[line.getBufferSize()/5];
            FileOutputStream out = new FileOutputStream(recordedFile);
            line.start();

        
            while(recState==RecordState.RECORDING){
                numBytesRead = line.read(data, 0, data.length);
                out.write(data,0,numBytesRead);
            }
            */
        }
        catch(Exception ex){
            Tracer.getInstance().trace(ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al grabar audio", JOptionPane.ERROR_MESSAGE);
            this.dispose();
        }
    }
    
    private void performSend(){
        if(ctrl != null){
           ctrl.sendFile(recordedFile, "");
        }
        focus();
    }
    
    public void setClientControllerInstance(ClientController ctrl){
        this.ctrl = ctrl;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        audioPanel = new GUI.AudioPanel();
        btRec = new javax.swing.JToggleButton();
        lblRec = new javax.swing.JLabel();
        btInfo = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();
        btSend = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        audioPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                audioPanelMouseClicked(evt);
            }
        });

        btRec.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Media/rec_icon.png"))); // NOI18N
        btRec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRecActionPerformed(evt);
            }
        });

        lblRec.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblRec.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Media/rec_icon.png"))); // NOI18N
        lblRec.setText("Grabando...");

        btInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Media/info_icon.png"))); // NOI18N
        btInfo.setToolTipText("Información detallada");
        btInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btInfoActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Grabar:");

        lblTime.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblTime.setText("0:00:00");

        btSend.setBackground(new java.awt.Color(0, 204, 102));
        btSend.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btSend.setForeground(new java.awt.Color(255, 255, 255));
        btSend.setText("SEND");
        btSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSendActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(37, 37, 37)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblRec, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTime, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btRec, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btSend))
                    .addComponent(audioPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btRec, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jLabel1)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblRec, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTime)))
                    .addComponent(btSend, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(audioPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btRecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRecActionPerformed
        performRec();
    }//GEN-LAST:event_btRecActionPerformed

    private void btInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btInfoActionPerformed
        String infoRec = "Utiliza el botón rojo para grabar el audio que desees.\n"+
                "Vuelve a pulsarlo para terminar la grabación.\n"+
                "También puedes utilizar las teclas Intro o Espacio.\n"+
                "Pulsando Intro para terminar la grabación el mensaje se enviará automáticamente.";
        
        JOptionPane.showMessageDialog(this, infoRec, null, JOptionPane.INFORMATION_MESSAGE);
        focus();
    }//GEN-LAST:event_btInfoActionPerformed

    private void audioPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_audioPanelMouseClicked
        focus();
    }//GEN-LAST:event_audioPanelMouseClicked

    private void btSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSendActionPerformed
        performSend();
    }//GEN-LAST:event_btSendActionPerformed

   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private GUI.AudioPanel audioPanel;
    private javax.swing.JButton btInfo;
    private javax.swing.JToggleButton btRec;
    private javax.swing.JButton btSend;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblRec;
    private javax.swing.JLabel lblTime;
    // End of variables declaration//GEN-END:variables
}
