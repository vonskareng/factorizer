/* Rebecka Skareng */ 

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;

public class Factorizer implements Runnable {

	private static final Object lock = new Object();
	private static boolean found;
	private BigInteger product;
	private static BigInteger factor1, factor2;
	private BigInteger min = new BigInteger("2");
	private BigInteger max;
	private BigInteger step;

	public Factorizer(BigInteger product, int numberOfThreads, int incrementer) {
		this.product = product;
		step = BigInteger.valueOf(numberOfThreads);
		max = product.subtract(new BigInteger("1"));
		min = min.add(BigInteger.valueOf(incrementer));
	}

	/**
	 * Anv�nder sig av en statisk boolean f�r att kunna avsluta 
	 * andra tr�dars run metod ifall en tr�d har hittat ett svar. Dock anv�nds
	 * bara l�s vid skrivning till denna variabel (samt de statiska produktvariablerna)
	 * vilket inneb�r en risk att tr�dar inte l�ser det senast uppdaterade v�rdet av "found".
	 * Men jag v�ljer att inte anv�nda ett l�s vid l�sning av "found" vid varje varv eftesom
	 * att synkronisera varje varv i loopen ger en prestandaf�rs�mring vid anv�ndning av mer �n tv� tr�dar
	 * och programmet ska vara effektivare ju fler tr�dar som anv�nds upp till datorns antal k�rnor. Nackdelen
	 * �r att en tr�d kan utf�ra on�diga uttr�kningar om den inte f�r r�tta v�rdet men ska programmet
	 * vara effektivare med fler k�rnor s� f�r man leva med detta. L�set som anv�nds �r statiskt och final
	 * vilket inneb�r att det �r of�r�ndligt s� att alla instanser av Factorizer delar l�s (s� att man kan ha flera instanser p� olika tr�dar).
	 */
	@Override
	public void run() {
		BigInteger number = min;
		while (number.compareTo(max) <= 0 && !found) {


			if (product.remainder(number).compareTo(BigInteger.ZERO) == 0) {

				BigInteger temp = product.divide(number);

				synchronized (lock) {
					if (found) {
						return;
					} else {
						factor1 = number;
						factor2 = temp;
						found = true;
					}
				}

			}
			number = number.add(step);
		}

	}

	public static void main(String[] args) {
		InputStreamReader streamReader = new InputStreamReader(System.in);
		BufferedReader consoleReader = new BufferedReader(streamReader);
		System.out.print("Antal tr�dar: ");
		String input;
		try {
			input = consoleReader.readLine();
			int numThreads = Integer.parseInt(input);
			System.out.print("Produkt: ");
			input = consoleReader.readLine();
			BigInteger product = new BigInteger(input);

			long start = System.nanoTime();

			Thread[] threads = new Thread[numThreads];
			Factorizer[] factorizers = new Factorizer[numThreads];
			for (int i = 0; i < numThreads; i++) {
				factorizers[i] = new Factorizer(product, numThreads, i);
				threads[i] = new Thread(factorizers[i]);
			}

			for (int i = 0; i < numThreads; i++) {
				threads[i].start();
			}

			for (int i = 0; i < numThreads; i++) {
				threads[i].join();
			}

			long stop = System.nanoTime();

			if (!Factorizer.found) {
				System.out.println("No factorization possible");
			} else {
				System.out.println(Factorizer.factor1 + " " + Factorizer.factor2);
				System.out.println("K�rtid (sekunder): " + (stop - start) / 1.0E9);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
