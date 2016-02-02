/*
 * Author: Juan Luis Suárez Díaz
 * July, 2015
 * Music Player
 */
package Main;

import Model.Client;
import Model.Server;

/**
 *
 * @author Juan Luis
 */
public class Main {
    public static void main(String[] args){
        String openMode = "client"; //Por defecto se ejecuta el cliente.
        String defaultMsg = "Por defecto se ejecuta el cliente de No More Dropbox MSN."+
                    " Para ver otros posibles modos de uso ejecute este programa con el argumento HELP.";
        if(args.length <= 0){
            System.out.println(defaultMsg);
        }
        else{
            openMode = args[0];
        }
        switch(openMode.toLowerCase()){
            case "client":
                Client.main(null);
                break;
            case "server":
                Server.main(null);
                break;
            case "tutor":
                System.out.println("Not implemented yet.");
                break;
            case "help":
            default:
                System.out.println(
                        "USO: NoMoreDropboxMSN <modo>\nMODOS:"+
                        "\n\t CLIENT: ejecuta el cliente."+
                        "\n\t SERVER: ejecuta el servidor."+
                        "\n\t TUTOR: abre la interfaz grafica de inicialización."+
                        "\n\n"+defaultMsg);
                break;
        }
    }
}
