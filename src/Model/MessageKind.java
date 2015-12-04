////////////////////////////////////////////////////////////////////////////////
// Author: Juan Luis Suarez Diaz
// Jun, 2015
// Dropbox MSN
////////////////////////////////////////////////////////////////////////////////
package Model;

/**
 * MessageKind enum.
 * Displays every way a message can take.
 * @author Juan Luis
 */
public enum MessageKind {
    HELO,LOGIN,SEND,CHANGEPRIVATE,CHANGESELECT,CHANGESTATE,IMALIVE,OK,ERR,BYE,
    RECEIVEMSG,RECEIVEUSR,LOGOUT,CONFIRMPRV,CONFIRMSLCT,CONFIRMSTATE;
}
