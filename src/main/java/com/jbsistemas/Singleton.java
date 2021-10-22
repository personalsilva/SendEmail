/**
 * Control de paquetes general
 */
package com.jbsistemas;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta clase captura los mensajes del proceso de env&iacute;o de correos y los
 * escribe en un log externo. El prop&oacute;sito de &eacute;ste log es poder
 * visualizar los mensajes output si no se ejecuta por l&iacute;nea de comandos.
 *
 * @author David Cruz Jim&eacute;nez
 * @author Daniel Torres Silva
 * @version 2.0
 * @since 1.0
 */
public class Singleton {

    private static final Singleton inst = new Singleton();

    /**
     * Llamado a propiedades del constructor general.
     */
    private Singleton() {
        super();
    }

    /**
     * Permite generar un registro de datos en el log externo SendEmail. El
     * archivo se escribe en la misma carpeta donde se ejecuta el componente
     * SendEmail.
     *
     * @param str Cadena de carateres {@code String} que se escribir&aacute; en
     * el log
     * @param apnd Si se especifica <code>true</code>, la siguiente cadena
     * ser&aacute; escrita debajo del resto de datos existentes sin crear un
     * flujo nuevo
     * @see java.io.FileOutputStream
     * @see java.io.File
     * @see java.io.PrintWriter
     */
    protected synchronized void writeToFile(String str, boolean apnd) {
        try (PrintWriter out = new PrintWriter(new FileOutputStream(new File("SendEmail.log"), apnd))) {
            out.println(str);
            out.flush();
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Singleton.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Obtiene una instancia de tipo <code>Singleton</code>. Se requiere para
     * obtener el acceso a escribir el archivo log.
     *
     * @return Una instancia de tipo {@code Singleton}
     */
    protected static Singleton getInstance() {
        return inst;
    }

}
