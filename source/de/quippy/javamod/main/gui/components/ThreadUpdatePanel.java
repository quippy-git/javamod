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

import javax.swing.JPanel;

import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * @since 09.09.2009
 */
public abstract class ThreadUpdatePanel extends JPanel
{
	private static final long serialVersionUID = 499420014207584726L;

	private volatile boolean threadRunning;
	private volatile int pause; // 0:nothing, 1:request, 2:in Pause
	private final int desiredFPS;
	private final MeterUpdateThread uiUpdateThread;
	private final long nanoWait;

	private static final class MeterUpdateThread extends Thread
	{
		private final ThreadUpdatePanel me;
		
		public MeterUpdateThread(ThreadUpdatePanel me)
		{
			super();
			this.me = me;
			this.setName("ThreadUpdatePanel::" + me.getClass().getName());
			this.setDaemon(true);
			this.setPriority(Thread.MAX_PRIORITY);
		}
		/**
		 * Will do the Update of this... 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			while (me.threadRunning)
			{
				final long now = System.nanoTime();
				try
				{
					me.doThreadUpdate();
				}
				catch (Throwable ex)
				{
					Log.error(this.getName(), ex);
				}
				final long stillToWait = me.nanoWait - (System.nanoTime() - now);
				if (stillToWait>0)
				{
					try { Thread.sleep(stillToWait/1000000L); } catch (InterruptedException ex) { /*noop*/ }
				}
				else
				{
					try { Thread.sleep(1L); } catch (InterruptedException ex) { /*noop*/ }
				}
				if (me.pause==1)
				{
					me.pause=2;
					while (me.pause==2) try { Thread.sleep(1L); } catch (InterruptedException ex) { /*noop*/ }
				}
			}
		}
	}

	/**
	 * Constructor for ThreadUpdatePanel
	 */
	public ThreadUpdatePanel(int desiredFPS)
	{
		super();
		this.desiredFPS = desiredFPS;
		this.nanoWait = 1000000000L / (long)desiredFPS;
		this.uiUpdateThread = new MeterUpdateThread(this);
	}
	/**
	 * Will start the Thread
	 * @since 01.01.2008
	 */
	protected void startThread()
	{
		threadRunning = true;
		uiUpdateThread.start();
	}
	public void pauseThread()
	{
		if (pause==0)
		{
			pause = 1;
			while (pause==1) try { Thread.sleep(1L); } catch (InterruptedException ex) { /*NOOP */ }
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
	protected void stopThread()
	{
		threadRunning = false;
	}
	/**
	 * @since 11.09.2009
	 * @return
	 */
	public int getDesiredFPS()
	{
		return desiredFPS;
	}
	/**
	 * Implement the regulary updates to be done
	 * (i.e. Peak Meter fall downs etc.)
	 * @since 01.01.2008
	 */
	protected abstract void doThreadUpdate();
}
