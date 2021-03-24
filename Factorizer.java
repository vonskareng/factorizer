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
	 * Använder sig av en statisk boolean för att kunna avsluta 
	 * andra trådars run metod ifall en tråd har hittat ett svar. Dock används
	 * bara lås vid skrivning till denna variabel (samt de statiska produktvariablerna)
	 * vilket innebär en risk att trådar inte läser det senast uppdaterade värdet av "found".
	 * Men jag väljer att inte använda ett lås vid läsning av "found" vid varje varv eftesom
	 * att synkronisera varje varv i loopen ger en prestandaförsämring vid användning av mer än två trådar
	 * och programmet ska vara effektivare ju fler trådar som används upp till datorns antal kärnor. Nackdelen
	 * är att en tråd kan utföra onödiga utträkningar om den inte får rätta värdet men ska programmet
	 * vara effektivare med fler kärnor så får man leva med detta. Låset som används är statiskt och final
	 * vilket innebär att det är oförändligt så att alla instanser av Factorizer delar lås (så att man kan ha flera instanser på olika trådar).
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
		System.out.print("Antal trådar: ");
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
				System.out.println("Körtid (sekunder): " + (stop - start) / 1.0E9);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
