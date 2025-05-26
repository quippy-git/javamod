/*
 * @(#) CircularBuffer.java
 *
 * Created on 13.11.2023 by Daniel Becker
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
package de.quippy.javamod.system;

import java.io.Serializable;

/**
 * I was not able to find a standard FIFO stack implementation that would not
 * grow automatically, so I made my own very basic implementation.<br>
 * This implementation will not grow. If the size is not sufficient, no more
 * pushes will be possible. However, you can in that case call "growBy".<br>
 * The PushPointer always points to the next empty entry, the popPointer to
 * the next element to be popped. With peek you can do a look-ahead.<br>
 * It is defined empty, when push- and popPointer are on the same Index. It is defined
 * full, when the pushPointer + 1 would fall on popPointer.<br>
 * That means, that we can only fill (size - 1) elements into the queue.<br><br>
 * This implementation is <b>not</b> thread-safe, but we do want to be fast - so no
 * synchronized blocks or method. Giving it a bit of safety by declaring vars
 * as volatile.
 * @author Daniel Becker
 * @since 13.11.2023
 */
public class CircularBuffer<E> implements Serializable
{
	private static final long serialVersionUID = 5285069332735206260L;

	private volatile Object [] elements;
	private volatile int popPointer;
	private volatile int pushPointer;
	private int size;

	/**
	 * Constructor for CircularBuffer
	 */
	public CircularBuffer(final int capacity)
	{
		super();
		elements = new Object[size = capacity];
		popPointer = pushPointer = 0;
	}
	/**
	 * Constructor for CircularBuffer with a default of 64 elements
	 */
	public CircularBuffer()
	{
		this(64);
	}
	/**
	 * returns the size of the buffer in total
	 * @since 13.11.2023
	 * @return
	 */
	public int getBufferSize()
	{
		return size;
	}
	/**
	 * We will resize the buffer by either adding new space at the end (i.e. pushPointer
	 * is greater or equal to popPointer) or we will enlarge the space between
	 * push- and popPointer.<br>
	 * This operation is not thread safe! If one thread pushes or resizes and another one
	 * is popping, this <i>will</i> result in unforseeable results.
	 * @since 02.12.2023
	 * @param addSize
	 */
	public void growBy(final int addSize)
	{
		final Object [] newBuffer = new Object[size + addSize];

		if (pushPointer >= popPointer) // just add new space at the end
		{
			System.arraycopy(elements, 0, newBuffer, 0, size);
		}
		else // enlarge space between push- and popPointer
		{
			// copy pop-able elements to the end
			final int popElementsTillWrap = size - popPointer;
			final int newPopPointer = newBuffer.length-popElementsTillWrap;
			System.arraycopy(elements, popPointer, newBuffer, newPopPointer, popElementsTillWrap);
			popPointer = newPopPointer;
			// and copy the rest to the front
			System.arraycopy(elements, 0, newBuffer, 0, pushPointer);
		}
		elements = newBuffer;
		size = elements.length;
	}
	/**
	 * Returns the amount of elements queued.
	 * @since 13.11.2023
	 * @return
	 */
	public int getSize()
	{
		return (pushPointer>=popPointer) ? pushPointer - popPointer : size - popPointer + pushPointer;
	}
	/**
	 * Returns the amount of elements that can be queued
	 * @since 13.11.2023
	 * @return
	 */
	public int getFree()
	{
		return (pushPointer>=popPointer) ? size - pushPointer + popPointer - 1 : popPointer - pushPointer - 1;
	}
	/**
	 * Flush the buffer - i.e. pushPointer and popPointer are set to zero
	 * @since 13.11.2023
	 */
	public void flush()
	{
		pushPointer = popPointer = 0;
	}
	/**
	 * Return true, if buffer is empty
	 * @since 13.11.2023
	 * @return
	 */
	public boolean isEmpty()
	{
		return popPointer==pushPointer;
	}
	/**
	 * Return true, if buffer is full, i.e. a push would land on popPointer
	 * @since 13.11.2023
	 * @return
	 */
	public boolean isFull()
	{
		return ((pushPointer + 1) % size)==popPointer;
	}
	/**
	 * Return null, if list is empty - otherwise next element in queue
	 * @since 13.11.2023
	 * @return null, if list is empty
	 */
	@SuppressWarnings("unchecked")
	public E pop()
	{
		if (isEmpty()) return null;

		final E element = (E)elements[popPointer];
		elements[popPointer] = null; // delete the popped element
		popPointer = (popPointer + 1) % size;
		return element;
	}
	/**
	 * Look ahead for popPointer plus "add" elements. "add" can be 0 to peek the current
	 * "pop-able" value.
	 * If the queue contains any value, the whole list can be iterated, even
	 * beyond pushPointer. No checks done.
	 * @since 16.11.2023
	 * @param add
	 * @return null, if list is empty
	 */
	@SuppressWarnings("unchecked")
	public E peek(final int add)
	{
		if (isEmpty()) return null;

		return (E)elements[(popPointer + add) % size];
	}
	/**
	 * Add an element. If there is no space left, we will do nothing. You may
	 * check that in advance and call "growBy" to add more space.
	 * @since 13.11.2023
	 * @param element
	 */
	public void push(final E element)
	{
		if (isFull()) return;

		elements[pushPointer] = element;
		pushPointer = (pushPointer + 1) % size;
	}
	/**
	 * Give us a String representation of this Object. That is:<br>
	 * pushPointer / popPointer<br>
	 * # values queued / # free space<br>
	 * the pushed elements (max 10 of them)
	 * @return A String representation of this Queue
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder('{');
		sb.append(String.valueOf(pushPointer)).append('/').append(String.valueOf(popPointer)).append(',')
			.append(String.valueOf(getSize())).append('/').append(String.valueOf(getFree()))
			.append(", {");
		if (elements==null) sb.append("NULL");
		else
		{
			final int printMe = (size<10)?size:10;
			for (int i=0; i<printMe; i++)
			{
				if (i>0) sb.append(',');
				if (elements[i]==null) sb.append("NULL"); else sb.append(elements[i].toString());
			}
			if (size>printMe) sb.append(",...");
		}
		return sb.append("}}").toString();
	}
}
