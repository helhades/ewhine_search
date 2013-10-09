package com.ewhine.util;

import cn.gov.cbrc.wh.log.Log;
import cn.gov.cbrc.wh.log.LogFactory;

public class TimerTask {
	
	final private static Log log = LogFactory.getLog(TimerTask.class);
	private Runnable runnable;
	private long cycleTime;
	private boolean running = false;
	private Thread clockThread;
	private String name;

	public TimerTask(long cycleTime, Runnable run,String name) {

		this.runnable = run;
		this.cycleTime = cycleTime;
		this.name = name;

	}

	public void start() {

		running = true;

		clockThread = new Thread(new Runnable() {

			public void run() {

				synchronized (this) {
					while (running) {
						try {
							if (runnable != null) {
								runnable.run();
							}	

						} catch (Throwable e) {
							if (log.isErrorEnabled()) {
								e.printStackTrace();
								log.error("Auto task thread error!", e);
							}
						}
						
						try {
							this.wait(cycleTime);
						} catch (InterruptedException e) {
							return;
						}

					}
				}

			}

		},name);

		clockThread.start();

	}

	public void stop() {

		synchronized (this) {

			if (running == false) {
				return;
			}

			running = false;
			this.notifyAll();
		}

		if (clockThread != null) {
			try {
				if (log.isInfoEnabled()) {
					log.info(runnable.getClass().getName()
							+ " waiting thread stopping...!");
				}
				clockThread.join();
			} catch (InterruptedException e) {

			}
		}
	}

	public static void main(String[] args) {
		TimerTask task = new TimerTask(5000, new Runnable() {

			public void run() {
				System.out.println("do work!");

			}

		},"name");

		task.start();

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		task.stop();

	}

}
