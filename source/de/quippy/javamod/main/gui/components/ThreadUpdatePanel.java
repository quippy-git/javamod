/*
 * @(#) ThreadUpdatePanel.java
 *
 * Created on 09.09.2009 by Daniel Becker
 * 
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
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
package de.quippy.javamod.main.gui.components;

import javax.swing.JComponent;

import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * @since 09.09.2009
 */
public abstract class ThreadUpdatePanel extends JComponent
{
	private static final long serialVersionUID = 499420014207584726L;

	protected int desiredFPS;

	private volatile boolean threadRunning;
	private volatile int pause; // 0:nothing, 1:request, 2:in Pause
	private final MeterUpdateThread uiUpdateThread;

	private static final class MeterUpdateThread extends Thread
	{
		private long nanoFPS;
		private final ThreadUpdatePanel me;
		
		public MeterUpdateThread(ThreadUpdatePanel me, final long desiredFPS)
		{
			super();
			this.me = me;
			nanoFPS = 1000000000L / desiredFPS;
			
			setName("ThreadUpdatePanel::" + me.getClass().getName());
			setDaemon(true);
			//setPriority(Thread.MAX_PRIORITY);
		}
		/**
		 * Will do the Update of this... 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			long additionalWait = 0;
			while (me.threadRunning)
			{
				final long now = System.nanoTime();
				
				long stillToWait = nanoFPS - additionalWait;
				if (stillToWait<=0)
					stillToWait = 0L;
				else
					try { Thread.sleep(stillToWait/1000000L); } catch (InterruptedException ex) { /*noop*/ }
				
				try
				{
					me.doThreadUpdate();
				}
				catch (Throwable ex)
				{
					Log.error(this.getName(), ex);
				}
				
				if (me.pause==1)
				{
					me.pause=2;
					while (me.pause==2) try { Thread.sleep(10L); } catch (InterruptedException ex) { /*noop*/ }
				}
				
				additionalWait = System.nanoTime() - now - stillToWait;
			}
		}
	}

	/**
	 * Constructor for ThreadUpdatePanel
	 */
	public ThreadUpdatePanel(int desiredFPS)
	{
		super();
		setDoubleBuffered(true);
		this.desiredFPS = desiredFPS;
		uiUpdateThread = new MeterUpdateThread(this, desiredFPS);
	}
	/**
	 * Will start the Thread
	 * @since 01.01.2008
	 */
	public void startThread()
	{
		if (!threadRunning)
		{
			threadRunning = true;
			uiUpdateThread.start();
		}
	}
	public void pauseThread()
	{
		if (threadRunning && pause==0) // not paused and running
		{
			pause = 1; // move into status isPaused
			// wait for pause to reach status 2 (isPaused)
			while (pause==1) try { Thread.sleep(10L); } catch (InterruptedException ex) { /*NOOP */ }
		}
	}
	public void unPauseThread()
	{
		pause = 0;
	}
	/**
	 * Will stop this Thread
	 * @since 01.01.2008
	 */
	public void stopThread()
	{
		if (threadRunning)
		{
			threadRunning = false;
			unPauseThread();
		}
	}
	/**
	 * @since 11.09.2009
	 * @return -1 if not FPS is set
	 */
	public int getDesiredFPS()
	{
		return desiredFPS;
	}
	/**
	 * Implement the regular updates to be done
	 * (i.e. Peak Meter fall downs etc.)
	 * @since 01.01.2008
	 */
	protected abstract void doThreadUpdate();
}
