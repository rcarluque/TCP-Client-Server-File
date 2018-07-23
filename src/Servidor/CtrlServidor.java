/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author crafa
 */
public class CtrlServidor {
    private JFrameServidor vista;
    
    private File file;
    private ServerSocket serverSoc;
    private DataInputStream entrada;
    private DataOutputStream salida;

    public CtrlServidor(JFrameServidor vista) {
        this.vista = vista;
    }
    
    public void seleccionArchivoPincipal() {
        file = vista.lanzaChooserPrincipal();
    }
    
    public void seleccionArchivo() {
        file = vista.lanzaChooser();
    }
    
    public void enviaFichero(String puerto) {        
        if(file != null) {
            try {
                serverSoc = new ServerSocket(Integer.parseInt(puerto));
                String control;
                System.out.println("Esperando a una petición...");
                Socket socket = serverSoc.accept();
                vista.setTexToArea("Conectando con: " + socket.getInetAddress().toString());
                
                entrada = new DataInputStream(socket.getInputStream());
                salida = new DataOutputStream(socket.getOutputStream());
                
                // leemos desde el cliente la variable de control (debe tener de valor 'start')
                control = entrada.readUTF();
                
                while(!control.equals("stop")) {
                    vista.setTexToArea("Enviando archivo..." + file.getName());
                    // Escribimos el nombre del archivo
                    salida.writeUTF(file.getName());
                    salida.flush();
                    
                    // Escribimos la longitud del archivo.
                    long tamano = (int) file.length();
                    salida.writeUTF(Long.toString(tamano));
                    salida.flush();
                    
                    // Mostramos la información
                    vista.setTexToArea("Tamaño: "+tamano);
                    vista.setTexToArea("Tamaño Buffer: " +serverSoc.getReceiveBufferSize());
                    
                    // Preparamos el envio del fichero.
                    FileInputStream fis = new FileInputStream(file);
                    // byte buffer[] = new byte[(int) file.length()];
                    byte buffer[] = new byte[1024];
                    int count;
                    
                    while ( (count = fis.read(buffer)) != -1) {
                        salida.write(buffer, 0, count);
                        salida.flush();
                    }
                    fis.close();
                    salida.flush();
                    // Una vez que finalize la transferencia, paramos el programa.
                    control = "stop";
                    vista.setTexToArea("Transferencia completada.");
                    System.out.println("Transferencia completa.");
                    
                    socket.close();
                    serverSoc.close();
                }
                
            } catch (BindException be) {
                vista.msgAdressUse();    
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
}