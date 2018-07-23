/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Cliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.swing.JFileChooser;

/**
 *
 * @author crafa
 */
public class CtrlCliente {
    private JFrameCliente vista;
    
    private Socket socket = null;
    private DataInputStream entrada = null;
    private DataOutputStream salida = null;
    private FileOutputStream fos = null;
    private String rutaArchivo, nombreArchivo;
    
    public CtrlCliente(JFrameCliente vista) {
        this.vista = vista;
    }
    
    public boolean setDirectory() {
        File f;
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int seleccion = fc.showOpenDialog(vista);

        if(seleccion == JFileChooser.APPROVE_OPTION){
            f = fc.getSelectedFile();
            rutaArchivo = f.getAbsolutePath();
            return true;
        }
        return false;
    }
    
    public void reciveFile() {
        try {
            socket = new Socket(vista.getIp(), vista.getPort());
            System.out.println("Esperando al sevidor...");
            salida = new DataOutputStream(socket.getOutputStream());
            entrada = new DataInputStream(socket.getInputStream());
            
            // Escribimos al servidor, para que recoja una variable de control, y así sabe que debe empezar al transferencia.
            salida.writeUTF("start");
            salida.flush();
            
            // recibimos el nombre del fichero.
            nombreArchivo = entrada.readUTF();
            vista.setTextToArea("Recibiendo archivo..." +nombreArchivo);
            
            // recibimos la longitud del fichero.
            long length = Long.parseLong(entrada.readUTF());
            vista.setTextToArea("Tamaño del fichero: " + (length / (1024 * 1024)) +"MB");
            // Recogemos los bytes.
            byte buffer[] = new byte[1024];
            // Escritura en fichero.
            fos = new FileOutputStream(new File(rutaArchivo+"/"+nombreArchivo), true);
            long bytesLeidos;
            
            do {
                // Leemos los bytes del servidor y los almacenamos en el buffer,
                // que a su vez los almacenará en bytesLeidos, para comprobar cuando cerrar
                // el bucle. Por último escribimos en el fichero.
                bytesLeidos = entrada.read(buffer, 0, buffer.length);
                fos.write(buffer, 0, buffer.length);
            } while(!(bytesLeidos < 1024));
            vista.setTextToArea("Archivo guardado en: "+rutaArchivo);
            
            vista.setTextToArea("Se ha completado la transferencia.");
            System.out.println("Transferencia completa");
            
            // Cerramos
            fos.close();
            salida.close();
            entrada.close();
            socket.close();
            
        } catch (ConnectException ce) {
            vista.msgNoConecta();
        } catch (SocketException se) {
            vista.msgNoConecta();
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch(UnknownHostException uhe) {
            vista.msgNoHost();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
}
