/*
 * @(#) SongUpdater.java
 *
 * Created on 12.01.2024 by Daniel Becker
 * 
 * This class will provide the thread to time the fired Mixer-Updates to
 * the play back so that regardless of the buffer size (i.e. latency) set
 * the display of the information is on point.
 * It is inbetween of the mixer and the pattern-, instrument- and sample
 * dialogs.
 * The wiring is done in the ModContainer
 * 
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */
package de.quippy.javamod.multimedia.mod.gui;

import java.util.ArrayList;

import de.quippy.javamod.system.CircularBuffer;

/**
 * @author Daniel Becker
 * @since 12.01.2024
 */
public class SongUpdater implements ModUpdateListener
{
	/**
	 * This thread will update the pattern view and show the current pattern
	 * plus highlight the current row.
	 * We use a thread approach, because we do not want the mixing to be
	 * interrupted by a listener routine doing arbitrary things. This way that 
	 * is decoupled. Furthermore, we will synchronize to the time index send
	 * with each event - and that is done best in this local thread.
	 * 
	 * Whoever wants to be informed needs to implement the ModUpdateListener
	 * interface and register at BasicModMixer::registerUpdateListener
	 * 
	 * The linkage of ModPatternDialog and this songFollower is done in the
	 * ModContainer::createNewMixer. When a new mod mixer is created, we will
	 * first de-register the previous one and stop the songFollower thread - 
	 * but not earlier.  
	 * 
	 * In ModMixer::startPlayback we will toggle the fireUpdates-Flag in
	 * BasicModMixer to prevent updates fired when we do not want to get
	 * informed of any.
	 * 
	 * @author Daniel Becker
	 * @since 11.11.2023
	 */
	private class SongFollower extends Thread
	{
		private static final int INITIAL_SIZE = 0x1000; // 64 channel with 750ms sound buffer needs a push buffer of approximately 0xF00 size.
//		private static final int GROW_BY_SIZE = 0x100;
		private CircularBuffer<TimedInformation> buffer;
		
		private volatile boolean running;
		private volatile boolean hasStopped;
		private volatile boolean updating;
		private volatile boolean paused;
		private volatile boolean isPaused;
		private volatile boolean drain;

		public SongFollower()
		{
			super();
			buffer = new CircularBuffer<TimedInformation>(INITIAL_SIZE);
			running = true;
			hasStopped = false;
			updating = false;
			paused = isPaused = false;
			drain = false;
			setName("InformerThread");
			setDaemon(true);
//			try { this.setPriority(Thread.MAX_PRIORITY); } catch (SecurityException ex) { /*NOOP*/ }
		}
		/**
		 * Add an event from outside
		 * @since 13.11.2023
		 * @param information
		 */
		public void push(final TimedInformation information)
		{
			while (drain) try { Thread.sleep(10L); } catch (InterruptedException ex) { /*NOOP*/ }
			if (running)
			{
//				if (buffer.isFull()) buffer.growBy(GROW_BY_SIZE); // we do not want to grow as that is not thread safe. If events cannot be pushed, forget them!
				buffer.push(information);
			}
		}
		/**
		 * Invalidate all events in the queue
		 * @since 24.11.2023
		 */
		public void flush()
		{
			buffer.flush();
			while (updating) try { Thread.sleep(10L); } catch (InterruptedException ex) { /*NOOP*/ }
		}
		/**
		 * Will halt adding of new events and deliver / drain all remains
		 * in buffer. Method blocks, till all left over events are gone.
		 * This will also block the push method, till all is delivered.
		 * @since 29.11.2023
		 */
		public void drain()
		{
			drain = true;
			while (!buffer.isEmpty()) try { Thread.sleep(10L); } catch (InterruptedException ex) { /*NOOP*/ }
			drain = false;
		}
		/**
		 * This will stop the thread gracefully and halt it. After this call
		 * the thread is gone!
		 * @since 13.11.2023
		 */
		public void stopMe()
		{
			running = false;
			flush();
			while (!hasStopped) try { Thread.sleep(10L); } catch (InterruptedException ex) { /*NOOP*/ }
		}
		/**
		 * Will pause/unpause the thread
		 * @since 28.11.2023
		 * @param isPaused
		 */
		public void pause(final boolean doPause)
		{
			paused = doPause;
			while (paused!=isPaused) try { Thread.sleep(10L); } catch (InterruptedException ex) { /*NOOP*/ }
		}
		public void run()
		{
			long additionalWait = 0;
			long lastTimeCode = 0;
			hasStopped=false;

			while (running)
			{
				// wait for the first event to appear
				while (buffer.isEmpty() && running) try { Thread.sleep(1L); } catch (InterruptedException ex) { /*NOOP*/ }
				if (!running) break; // if we got stopped meanwhile, let's drop out... 
				
				while (!buffer.isEmpty())
				{
					final long startNanoTime = System.nanoTime();
					
					TimedInformation information = buffer.peek(0);
					
					final long nanoWait = ((information.timeCode - lastTimeCode) * 1000000L) - additionalWait;
					lastTimeCode = information.timeCode;
					if (nanoWait>0)
						try { Thread.sleep(nanoWait/1000000L); } catch (InterruptedException ex) { /*NOOP*/ }

					updating = true;
					while (!buffer.isEmpty() && ((TimedInformation)buffer.peek(0)).timeCode <= lastTimeCode)
					{
						information = buffer.pop();
						if (information!=null) SongUpdater.this.fireTimedInformation(information);
					}
					updating = false;

					// if this was the last event in the queue, wait for the next one - typically this is a pattern delay...
					while (buffer.isEmpty() && running) try { Thread.sleep(1L); } catch (InterruptedException ex) { /*NOOP*/ }

					if (paused) // if we should pause updates, wait here...
					{
						isPaused = true;
						while (paused && running) try { Thread.sleep(10L); } catch (InterruptedException ex) { /*NOOP*/ }
						isPaused = false;
					}
					if (!running) break; // if we got stopped meanwhile, let's drop out... 

					additionalWait = System.nanoTime() - (startNanoTime + nanoWait);
				}
			}
			hasStopped=true;
		}
	}

	// The UpdateListener Thread - to decouple whoever wants to get informed
	private SongFollower songFollower;
	// The listeners we will fire updates to
	private ArrayList<ModUpdateListener> listeners;

	/**
	 * Constructor for SongUpdater
	 */
	public SongUpdater()
	{
		super();
		listeners = new ArrayList<ModUpdateListener>();
	}
	/**
	 * Flush the update Thread buffer
	 * @since 24.11.2023
	 */
	public void flushUpdateThread()
	{
		if (songFollower!=null) songFollower.flush();
	}
	/**
	 * Drains all events left over. This will also block adding of new events
	 * till buffer is drained.
	 * @since 29.11.2023
	 */
	public void drainUpdateThread()
	{
		if (songFollower!=null) songFollower.drain();
	}
	/**
	 * @since 28.11.2023
	 * @param doPause
	 */
	public void pauseUpdateThread(final boolean doPause)
	{
		if (songFollower!=null) songFollower.pause(doPause);
	}
	/**
	 * Stop the thread
	 * @since 13.11.2023
	 */
	public void stopUpdateThread()
	{
		if (songFollower!=null)
		{
			songFollower.stopMe();
			songFollower = null;
		}
	}
	/**
	 * Create and start the Thread
	 * @since 13.11.2023
	 */
	public void startUpdateThread()
	{
		if (songFollower!=null) stopUpdateThread();
		songFollower = new SongFollower();
		songFollower.start();
	}
	public void registerUpdateListener(final ModUpdateListener listener)
	{
		if (listeners!=null && !listeners.contains(listener)) listeners.add(listener);
	}
	public void deregisterUpdateListener(final ModUpdateListener listener)
	{
		if (listeners!=null && listeners.contains(listener)) listeners.remove(listener);
	}
	private void firePatternPositionInformation(final PatternPositionInformation information)
	{
		if (listeners!=null && information!=null)
		{
			for (ModUpdateListener listener : listeners)
			{
				listener.getPatternPositionInformation(information);
			}
		}
	}
	private void firePeekInformation(final PeekInformation information)
	{
		if (listeners!=null && information!=null)
		{
			for (ModUpdateListener listener : listeners)
			{
				listener.getPeekInformation(information);
			}
		}
	}
	private void fireTimedInformation(final TimedInformation information)
	{
		if (information!=null)
		{
			if (information instanceof PatternPositionInformation)
				firePatternPositionInformation((PatternPositionInformation)information);
			else
			if (information instanceof PeekInformation)
				firePeekInformation((PeekInformation)information);
		}
	}
	public void fireInformationUpdate(final StatusInformation information)
	{
		if (listeners!=null && information!=null)
		{
			for (ModUpdateListener listener : listeners)
			{
				listener.getStatusInformation(information);
			}
		}
	}
	/**
	 * @param infoObject
	 * @see de.quippy.javamod.multimedia.mod.gui.ModUpdateListener#getPatternPositionInformation(de.quippy.javamod.multimedia.mod.gui.ModUpdateListener.PatternPositionInformation)
	 */
	@Override
	public void getPatternPositionInformation(PatternPositionInformation infoObject)
	{
		if (songFollower!=null && infoObject!=null) songFollower.push(infoObject);
	}
	/**
	 * @param infoObject
	 * @see de.quippy.javamod.multimedia.mod.gui.ModUpdateListener#getPeekInformation(de.quippy.javamod.multimedia.mod.gui.ModUpdateListener.PeekInformation)
	 */
	@Override
	public void getPeekInformation(PeekInformation infoObject)
	{
		if (songFollower!=null && infoObject!=null) songFollower.push(infoObject);
	}
	/**
	 * @param infoObject
	 * @see de.quippy.javamod.multimedia.mod.gui.ModUpdateListener#getStatusInformation(de.quippy.javamod.multimedia.mod.gui.ModUpdateListener.StatusInformation)
	 */
	@Override
	public void getStatusInformation(StatusInformation infoObject)
	{
		if (infoObject!=null)
		{
			if (!infoObject.status) drainUpdateThread();
			fireInformationUpdate(infoObject);
		}
	}
}
