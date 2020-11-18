
/*
Nombre: Ulises Bojórquez Ortiz
Mátricula: A01114716
Proyecto: Chat con Sockets e Hilos con encriptación Y desencriptación RSA
*/
import java.io.DataInputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.io.IOException;
import java.net.Socket;


import java.net.ServerSocket;

public class Servidor {
	//Puerto del servidor
	public static int puerto;
	// Socket del server
	private static ServerSocket socketServidor = null;
	// Socket del cliente
	private static Socket socketCliente = null;
	//Numero maximo de clientes
	private static final int maxClientes = 2;
	//Arreglo de hilos
	private static final hilosCliente[] hilos = new hilosCliente[maxClientes];
	//Algoritmo RSA
	private static RSA rsa;
	
	public static void main(String args[]) {
		
		//Puerto del servidor
		puerto= 2323;
		try {
			//Creamos el socket del servidor
			socketServidor = new ServerSocket(puerto);
		} catch (IOException e) {
			System.out.println(e);
		}

		/*
		 Crearemos el socket del cliente por cada conexion recibida y la pasaremos al arreglo hilosCliente
		 */
		while (true) {
			try {
				//Se inicia el socket del cliente
				socketCliente = socketServidor.accept();
				
				int i = 0;
				//Inicia cada hilo del cliente que ingresa
				for (i = 0; i < maxClientes; i++) {
					if (hilos[i] == null) {
						rsa=new RSA();
						//Metemos en el arreglo el nuevo hilo del cliente con el socket del cliente que ingreso y el arreglo de hilos
						(hilos[i] = new hilosCliente(socketCliente, hilos,rsa)).start();
						break;
					}
				}
				//Si el numero de clientes ingresaon es igual al maximo lo rechazamos
				if (i == maxClientes) {
					PrintStream enviar = new PrintStream(socketCliente.getOutputStream());
					enviar.println("El servidor esta lleno. Intenta más tarde.");
					enviar.close();
					socketCliente.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}


class hilosCliente extends Thread {
	
	//Nombre del cliente
	private String nombreCliente = null;
	//Lo que recibe de los demas clientes
	private DataInputStream recibir = null;
	//Lo que envia este cliente
	private PrintStream enviar = null;
	//Socket del cliente actual
	private Socket socketCliente = null;
	//Arreglo de los hilos
	private final hilosCliente[] hilos;
	//Numero maximo de clientes actuales
	private int maxClientes;
	//Encriptacion RSA
	private RSA rsa;

	
	public hilosCliente(Socket socketCliente, hilosCliente[] hilos, RSA rsa) {
		this.socketCliente = socketCliente;
		this.hilos = hilos;
		maxClientes = hilos.length;
		this.rsa=rsa;
	}
	

	@SuppressWarnings("deprecation")
	public void run() {
		
		int maxClientes = this.maxClientes;
		hilosCliente[] hilos = this.hilos;
		
		try {
			//El cliente actul recibe un mensaje
			this.recibir = new DataInputStream(this.socketCliente.getInputStream());
			//El cliente actual envia un mensaje
			this.enviar = new PrintStream(this.socketCliente.getOutputStream());
			
			//Nombre que le asginaremos a dicho cliente
			String nombre;
			while (true) {
				this.enviar.println("Escribe tu nombre:");
				nombre = this.recibir.readLine().trim();
				if (!nombre.isEmpty()) {
					break;
				} else {
					this.enviar.println("Ingresa correctamente tu nombre.");
				}
			}

			//Dar la bienvenida al nuevo cliente 
			this.enviar.println("Bienvenido " + nombre + " al chat.\nPara salir ingresa /bye.");
			synchronized (this) {
				for (int i = 0; i < maxClientes; i++) {
					if (this.hilos[i] != null && this.hilos[i] == this) {
						this.nombreCliente=nombre;
						System.out.println("Cliente <"+nombreCliente+">\n"
								+"n=" +this.rsa.getN()+"\n"
								+"Llave publica=" +this.rsa.getE()+"\n"
								+"Llave privada=" +this.rsa.getD()+"\n");
						
						break;
					}
				}
				//Enviamos a todos los clientes conectados
				for (int i = 0; i < maxClientes; i++) {
					if (this.hilos[i] != null && this.hilos[i] != this) {
						this.hilos[i].enviar.println("*** Un nuevo usuario " + nombre
								+ " a entrado al chat!!! ***");
					}
				}
			}
			//Inicia la conversacion
			while (true) {
				String line = this.recibir.readLine();
				BigInteger[] textoEncriptado= this.rsa.encriptar(line);
				String msjEncriptado="";
				for(int j=0;j<textoEncriptado.length;j++) {
					msjEncriptado+=textoEncriptado[j];
				}
				 
				if (line.startsWith("/bye")) {
					break;
				}

				//Enviar mensaje a todos los clientes
				synchronized (this) {
					String textoDesencriptado=this.rsa.desencriptar(textoEncriptado);
					for (int i = 0; i < maxClientes; i++) {
						if (hilos[i] != null && hilos[i].nombreCliente != null && hilos[i]!=this) {
							
							System.out.println("Mensaje recibido <" + hilos[i].nombreCliente + "> "+ msjEncriptado);
							this.hilos[i].enviar.println("<" + nombre + "> " + textoDesencriptado);
						}
					}
				}
			}
			
			//Cuando un usuario sale del chat
			synchronized (this) {
				for (int i = 0; i < maxClientes; i++) {
					if (this.hilos[i] != null && this.hilos[i] != this
							&& this.hilos[i].nombreCliente != null) {
						this.hilos[i].enviar.println("*** El usuario " + nombre
								+ " a salido del chat!!! ***");
					}
				}
			}
			enviar.println("########## Bye " + nombre + " ##########");

			//Hay que limpiar el hilo actual para nuevos clientes
			synchronized (this) {
				for (int i = 0; i < maxClientes; i++) {
					if (this.hilos[i] == this) {
						this.hilos[i] = null;
					}
				}
			}
			
			//Cerramos el input, output y el socket del cliente actual
			enviar.close();
			recibir.close();
			socketCliente.close();
		} catch (IOException e) {
		}
	}
}