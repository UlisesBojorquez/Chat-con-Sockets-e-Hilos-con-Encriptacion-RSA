

/*
Nombre: Ulises Bojórquez Ortiz
Mátricula: A01114716
Proyecto: Chat con Sockets e Hilos con encriptación Y desencriptación RSA
*/

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Cliente implements Runnable {
	
	//Puerto al que se dirige
	private static int puerto=2323;
	//Host
	private static String host="localhost";
	//Socket del cliente
	private static Socket socketCliente = null;
	// Output. Lo que manda a los demás
	private static PrintStream enviar = null;
	// Input. Lo que recibe
	private static DataInputStream recibir = null;
	//Lo que escribe en su consola el cliente 
	private static BufferedReader teclado = null;
	//Si se cerro el cliente
	private static boolean cerrado = false;

	public static void main(String[] args) {
		try {
			//Inicio el socket del cliente
			socketCliente = new Socket(host, puerto);
			//Inicio el teclado del cliente
			teclado = new BufferedReader(new InputStreamReader(System.in));
			//Inicio lo que manda el cliente
			enviar = new PrintStream(socketCliente.getOutputStream());
			//Inicio lo que recibo del cliente
			recibir = new DataInputStream(socketCliente.getInputStream());
			
			//Creo un Hilo para leer lo que envia el server
			new Thread(new Cliente()).start();
			while (!cerrado) {
				//Esto lo mando al server
				
				enviar.println(teclado.readLine().trim());
			}

			enviar.close(); //Se cierra el output
			recibir.close(); //Se cierra el input
			socketCliente.close(); //Se cierra el socket del cliente
		} catch (UnknownHostException e) {
			System.err.println("No se reconoce el host " + host);
		} catch (IOException e) {
			System.err.println("No se pudo hacer conexion con el host "+ host);
		}
	}

	@SuppressWarnings("deprecation")
	public void run() {

		String responseLine;
		try {
			//Mientras lo que reciba sea diferente de null
			while ((responseLine = recibir.readLine()) != null) {
				//Imprimelo
				System.out.println(responseLine);
				//Si lo que recibe es un ########## Bye del server
				if (responseLine.indexOf("########## Bye") != -1)
					//Termina este cliente
					break;
			}
			cerrado = true;
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		}
	}
}