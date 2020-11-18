

/*
Nombre: Ulises Bojórquez Ortiz
Mátricula: A01114716
Proyecto: Chat con Sockets e Hilos con encriptación Y desencriptación RSA

LINKS
https://juncotic.com/rsa-como-funciona-este-algoritmo/
 */

import java.math.BigInteger;
import java.util.Random;

public class RSA {
	
	private BigInteger p, //Primer numero primero
					   q, //Segundo numero primo
					   n, //multiplicacion de (p * q)
					   phi, // Phi(n) cantidad de numeros que tiene inversa para un modulo
					   e, //Clave publica
					   d; //Cñave privada
	private int primoSize;
	
	public RSA() {
		
		this.primoSize=512;
		
		this.p=new BigInteger(primoSize, 10, new Random());
		this.q=new BigInteger(primoSize, 10, new Random());
		
		//Obtenemos n. n=p*q
		this.n=p.multiply(q);
		//Obtenemos phi=(p-1)*(q-1)
		this.phi=p.subtract(BigInteger.valueOf(1)).multiply(q.subtract(BigInteger.ONE));

		do {
			this.e=new BigInteger(primoSize*2, new Random());
		//mientras e no sea menor a phi  o que el maximo comun divisor entre el numero e y el numero phi sea diferente de 1
		}while(this.e.compareTo(this.phi)!=-1 || this.e.gcd(this.phi).compareTo(BigInteger.valueOf(1)) !=0);
		this.d=this.e.modInverse(this.phi);
	}


	public synchronized BigInteger[] encriptar(String mensaje) {
		
		byte[] temp=new byte[1];
		
		//Esto guarda el mensaje en un arreglo den forma de bytes que lo representa el modelo ASCII
		byte[] digitos=mensaje.getBytes();
		BigInteger[] bigDigitos=new BigInteger[digitos.length];
		
		for(int i=0;i<bigDigitos.length;i++) {
			temp[0]=digitos[i];
			bigDigitos[i]=new BigInteger(temp);
		}
		
		BigInteger[] encriptado=new BigInteger[bigDigitos.length];
		for(int i=0;i<encriptado.length;i++) {
			//dato^emod(n)
			encriptado[i]=bigDigitos[i].modPow(this.e, this.n);
		}
		
		return encriptado;
		
		
	}
	
	public synchronized String desencriptar(BigInteger[] encriptado) {
		
		BigInteger[] desencriptado=encriptado;
		char[] arregloChar=new char[desencriptado.length];

		for(int i=0;i <desencriptado.length;i++) {
			//dato cigrado^dmod(n)
			desencriptado[i]=desencriptado[i].modPow(this.d, this.n);
			
			//Pasar de BigInteger a tipo char
			arregloChar[i]= (char) desencriptado[i].intValue();
			
		}
		System.out.println();
		
		//Regresa el mensaje encriptado usando la clase String para unir los char
		return String.valueOf(arregloChar);
		
	}
	
	public BigInteger getN() {
		return n;
	}


	public BigInteger getE() {
		return e;
	}


	public BigInteger getD() {
		return d;
	}

	
	public static void main(String[] args) {
	}

}
