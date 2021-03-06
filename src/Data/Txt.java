////////////////////////////////////////////////////////////////////////////////
// Author: Juan Luis Suarez Diaz
// Jun, 2015
// Dropbox MSN
////////////////////////////////////////////////////////////////////////////////
package Data;

/**
 * Class Txt.
 * Contains all remarkable texts related to the program.
 * @author Juan Luis
 */
public class Txt {
    /**
     * String with info about old versions.
     */
    public static final String OLD_VERSIONS_INFO =
            "Versión 1.0 (15/6/2015):"+
            "\n- Primer programa. Funcionalidades básicas del messenger e interfaz gráfica.\n\n"+
            "Versión 1.1 (17/6/2015):"+
            "\n- Añadido sonido al recibir un mensaje."+
            "\n- Resuelto el problema del scroll."+
            "\n- Resuelto el conflicto con la actualización del usuario."+
            "\n- Mejora en el ajuste del nombre de usuario.\n\n"+
            "Versión 1.2 (24/6/2015):"+
            "\n- Mejora en el manejo de los usuarios."+
            "\n- Añadida selección de mensajes (sin ningún uso por el momento)."+
            "\n- Añadidas fechas a los mensajes.\n\n"+
            "Versión 1.3 (5/7/2015):"+
            "\n- Añadidos colores al menú de estados de usuario."+
            "\n- Añadida la opción de enviar mensajes pulsando Intro.\n\n"+
            "Versión 1.4 (6/7/2015):"+
            "\n- Añadidas las opciones de copiar, pegar y borrar los mensajes seleccionados."+
            "\n- Ahora los mensajes copiados se añaden al portapapeles del sistema."+
            "\n- Añadido el menú de configuración.\n\n"+
            "Versión 2.0 (9/7/2015):"+
            "\n- Mejoras al enviar mensajes pulsando Intro."+
            "\n- Mejoras al habilitar el menú de configuración."+
            "\n- Añadida la opción de guardar mensajes."+
            "\n- Añadidos al programa los menús acerca del programa.\n\n"+
            "Versión 3.0 (6/12/2015):"+
            "\n- Nueva estructura cliente-servidor independiente de Dropbox."+
            "\n- El programa pasa a llamarse No More Dropbox MSN."+
            "\n- Servidor interactivo con línea de comandos."+
            "\n- Lectura de host y puerto modificable desde ficheros de configuración.\n\n"+
            "Versión 3.1 (22/12/2015):"+
            "\n- Añadidas notificaciones de escritorio."+
            "\n- Añadida configuración para las notificaciones de escritorio."+
            "\n- Solucionados algunos incidentes de desconexión."+
            "\n- Nuevos comandos en el servidor.\n\n"+
            "Versión 3.2 (2/2/2016):"+
            "\n- Ligeras mejoras en la interfaz de envío de mensajes."+
            "\n- Añadido el software para transferencia de archivos."+
            "\n- Añadidos mecanismos para la actualización automática del programa.\n\n"+
            "Versión 3.3 (5/7/2016):"+
            "\n- Añadido el envío de archivos."+
            "\n- Añadida visualización de archivos de audio, imagen y texto plano.\n\n"+
            "Versión 4.0 (17/8/2016):"+
            "\n- Reestructuración interna y mejora del protocolo de comunicación."+
            "\n- Mejora en el envío de archivos. Envíos concurrentes."+
            "\n- Solucionados errores con las desconexiones."+
            "\n- Añadidos nuevos mecanismos de depuración: clase rastreadora."+
            "\n- Nuevos comandos en el servidor."+
            "\n- Ligeras mejoras en la interfaz gráfica."+
            "\n- Cheats para depuración en cliente.\n\n"
            ;
    
    /**
     * String with info about last version.
     */
    public static final String LAST_VERSION_INFO =
        "Versión 4.1 (XX/X/2017):"+
        "\n- Añadido escalado de imágenes."+
        "\n- Añadido envío de notas de voz."+
        "\n\n"
    ;
    
    /**
     * Program's name
     */
    public static final String PROGRAM_NAME = "NO MORE DROPBOX MSN";
    
    /**
     * Author's string.
     */
    public static final String AUTHOR = "Juan Luis Suárez Díaz";
    
    /**
     * Version's data and compatibilities.
     */
    public static final double VERSION_CODE = 4.1;
    public static final double LAST_COMPATIBLE = 4.0;
    
    /**
     * Version's string.
     */
    public static final String VERSION = "v"+Double.toString(VERSION_CODE);
    
    /**
     * Copyright's string.
     */
    public static final String COPYRIGHT = "© 2015";
    
    /**
     * Edition's string.
     */
    public static final String EDITION = "No More Dropbox MSN Ultimate Java Edition";
    
    /**
     * String with program info.
     */
    public static final String PROGRAM_INFO = "";
}
