/**
 * Unlicensed code created by A Softer Space, 2022
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.mediaTextCreator;


public class TimingCtrl {

	private Long lastInputReceived;

	private GUI gui;


	public TimingCtrl(GUI gui) {
		this.gui = gui;
		setInputReceivedNow();
		startTimerThread();
	}

	public void setInputReceivedNow() {
		this.lastInputReceived = System.currentTimeMillis();
	}

	public Long getElapsedTimeSinceLastInputReceived() {
		return System.currentTimeMillis() - this.lastInputReceived;
	}

	public void setGui(GUI gui) {
		this.gui = gui;
		startTimerThread();
	}

	private void startTimerThread() {

		Thread timerThread = new Thread() {

			public void run() {

				while (true) {
					try {
						// after three seconds without input, restart playing
						if (getElapsedTimeSinceLastInputReceived() > 3000) {
							gui.startPlaying();
						}

						Thread.sleep(1000);

					} catch (InterruptedException e) {
						// just keep sleeping...
					}
				}
			}
		};
		timerThread.start();
	}

}
