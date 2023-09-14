package edu.repetita.solvers.sr.cg4sr.threading;

import edu.repetita.solvers.sr.cg4sr.SRTEColGen;
import edu.repetita.solvers.sr.cg4sr.data.Demand;
import edu.repetita.solvers.sr.cg4sr.data.Pair;
import edu.repetita.solvers.sr.cg4sr.data.Tuple4;
import edu.repetita.solvers.sr.cg4sr.segmentRouting.SrPath;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Mathieu Jadin mathieu.jadin@uclouvain.be
 */
public class PricingThread extends Thread {

	static private AtomicInteger runningThreads = new AtomicInteger(0);
	static private ReentrantLock lock = new ReentrantLock();
	static private AtomicInteger rdvThreads = new AtomicInteger(0);
	static private Semaphore pricingSemaphore = new Semaphore(0);

	private SRTEColGen colGen;
	private ArrayBlockingQueue<Tuple4<double[][], double[], Double, Integer>> processQueue;

	private Semaphore mainSem;
	private AtomicInteger columnAdded;
	private final int threadNumber;

	public PricingThread(SRTEColGen colGen, ArrayBlockingQueue<Tuple4<double[][], double[], Double, Integer>> processQueue,
	                     Semaphore mainSem, AtomicInteger columnAdded, int threadNumber) {
		this.colGen = colGen;
		this.processQueue = processQueue;
		this.threadNumber = threadNumber;
		this.mainSem = mainSem;
		this.columnAdded = columnAdded;
	}

	public void run() {
		while (true) {
			try {
				/* All threads must be started before processing paths of this iteration */
				lock.lock();
				int tmp = rdvThreads.incrementAndGet();
				if (tmp == threadNumber) {
					rdvThreads.set(0);
					runningThreads.set(threadNumber);
					pricingSemaphore.release(threadNumber);
				}
				lock.unlock();
				pricingSemaphore.acquire();

				/* Loop inside a single column generation iteration */
				while (true) {
					Tuple4<double[][], double[], Double, Integer> elem = processQueue.take();

					/* In case of poisoning at the end of one Colgen iteration or at the end of the program */
					if (elem.b == null && elem.c != null) {
						tmp = runningThreads.decrementAndGet();

						if (tmp == 0) { // The last thread wakes up the main thread
							mainSem.release();
							runningThreads.set(threadNumber);
						}
						break;
					} else if (elem.b == null && elem.c == null) {
						return;
					}
					double[][] w = elem.a;
					double[] edgeDual = elem.b;
					double demandDual = elem.c;
					int demandIndex = elem.d;
					Demand demand = this.colGen.getInstance().getDemand(demandIndex);
					Pair<SrPath, Double> path = this.colGen.pricingProblemMaxSeg(w, edgeDual, demandDual, demand);

					/* We found a new path, add it to the model */
					if (path != null) {
						lock.lock();
						try {
							this.colGen.getModel().addPath(demandIndex, path.x());
							columnAdded.incrementAndGet();
						} finally {
							lock.unlock();
						}
					}
				}
			} catch (InterruptedException e) {
				return;
			}
		}
	}
}
