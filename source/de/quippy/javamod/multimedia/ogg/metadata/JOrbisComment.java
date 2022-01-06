/*
 * @(#) OggMetaData.java
 *
 * Created on 01.11.2010 by Daniel Becker
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

package de.quippy.javamod.multimedia.ogg.metadata;

import java.io.InputStream;
import java.io.OutputStream;

import de.quippy.javamod.system.Log;
import de.quippy.ogg.jogg.Packet;
import de.quippy.ogg.jogg.Page;
import de.quippy.ogg.jogg.StreamState;
import de.quippy.ogg.jogg.SyncState;
import de.quippy.ogg.jorbis.Comment;
import de.quippy.ogg.jorbis.Info;

class State
{
	protected SyncState oy;
	protected StreamState os;
	protected Comment vc;
	protected Info vi;

	protected InputStream in;
	protected int serial;
	protected byte[] mainbuf;
	protected byte[] bookbuf;
	protected int mainlen;
	protected int booklen;
	protected String lasterror;
	protected long pcmLength;

	private int prevW;

	private Page og;
	
	public State()
	{
		super();
		og = new Page();
	}

	public int blocksize(Packet p)
	{
		int _this = vi.blocksize(p);
		int ret = (_this + prevW) / 4;

		if (prevW == 0)
		{
			prevW = _this;
			return 0;
		}

		prevW = _this;
		return ret;
	}

	public int fetch_next_packet(Packet p)
	{
		int result = os.packetout(p);

		if (result > 0) return 1;

		while (oy.pageout(og) <= 0)
		{
			int index = oy.buffer(JOrbisComment.CHUNKSIZE);
			byte [] buffer = oy.data;
			try
			{
				int bytes = in.read(buffer, index, JOrbisComment.CHUNKSIZE);
				if (bytes > 0) oy.wrote(bytes);
				if (bytes == 0 || bytes == -1)
				{
					return 0;
				}
			}
			catch (Exception e)
			{
				return 0;
			}
		}
		os.pagein(og);
		return fetch_next_packet(p);
	}
}

public class JOrbisComment
{
	protected static final int CHUNKSIZE = 4096;
	private State state = null;

	public JOrbisComment()
	{
		super();
		state = new State();
	}

	public Comment getComment()
	{
		return state.vc;
	}
	public int getLengthInMilliseconds()
	{
		if (state.vi!=null)
			return (int)(state.pcmLength * 1000 / state.vi.rate);
		else
			return 0;
	}
	public void read(InputStream in)
	{
		state.in = in;
		
		Page og = new Page();

		state.oy = new SyncState();
		state.oy.init();

		int index = state.oy.buffer(CHUNKSIZE);
		byte[] buffer = state.oy.data;
		int bytes = 0;
		try
		{
			bytes = state.in.read(buffer, index, CHUNKSIZE);
		}
		catch (Exception e)
		{
			Log.error("[JOrbisComment]", e);
			return;
		}
		state.oy.wrote(bytes);

		if (state.oy.pageout(og) != 1)
		{
			if (bytes < CHUNKSIZE)
			{
				Log.error("Input truncated or empty.");
			}
			else
			{
				Log.error("Input is not an Ogg bitstream.");
			}
			// goto err;
			return;
		}
		state.serial = og.serialno();
		state.os = new StreamState();
		state.os.init(state.serial);
		// os.reset();

		state.vi = new Info();
		state.vi.init();

		state.vc = new Comment();
		state.vc.init();

		if (state.os.pagein(og) < 0)
		{
			Log.error("Error reading first page of Ogg bitstream data.");
			// goto err
			return;
		}

		Packet header_main = new Packet();

		if (state.os.packetout(header_main) != 1)
		{
			Log.error("Error reading initial header packet.");
			// goto err
			return;
		}

		if (state.vi.synthesis_headerin(state.vc, header_main) < 0)
		{
			Log.error("This Ogg bitstream does not contain Vorbis data.");
			// goto err
			return;
		}

		state.mainlen = header_main.bytes;
		state.mainbuf = new byte[state.mainlen];
		System.arraycopy(header_main.packet_base, header_main.packet, state.mainbuf, 0, state.mainlen);

		int i = 0;
		Packet header_comments = new Packet();
		Packet header_codebooks = new Packet();

		Packet header = header_comments;
		while (i < 2)
		{
			while (i < 2)
			{
				int result = state.oy.pageout(og);
				if (result == 0)
					break; /* Too little data so far */
				else if (result == 1)
				{
					state.os.pagein(og);
					while (i < 2)
					{
						result = state.os.packetout(header);
						if (result == 0) break;
						if (result == -1)
						{
							Log.error("Corrupt secondary header.");
							// goto err;
							return;
						}
						state.vi.synthesis_headerin(state.vc, header);
						if (i == 1)
						{
							state.booklen = header.bytes;
							state.bookbuf = new byte[state.booklen];
							System.arraycopy(header.packet_base, header.packet, state.bookbuf, 0, header.bytes);
						}
						i++;
						header = header_codebooks;
					}
				}
			}

			index = state.oy.buffer(CHUNKSIZE);
			buffer = state.oy.data;
			try
			{
				bytes = state.in.read(buffer, index, CHUNKSIZE);
			}
			catch (Exception e)
			{
				Log.error("[JOrbisComment]", e);
				return;
			}

			if (bytes <= 0 && i < 2)
			{
				Log.error("EOF before end of vorbis headers.");
				// goto err;
				return;
			}
			state.oy.wrote(bytes);
		}
//		int repeat = 0;
		while (true)
		{
			/*int result = */state.oy.pageout(og);
			index = state.oy.buffer(CHUNKSIZE);
			buffer = state.oy.data;
			if (index==-1) 
			{
				state.pcmLength = og.granulepos();
				break;
			}
//			repeat++;
//			if (repeat>16) // Stop just once - no endless loop!! 
//			{
//				state.pcmLength = og.granulepos();
//				break;
//			}
			try
			{
				bytes = state.in.read(buffer, index, CHUNKSIZE);
			}
			catch (Exception e)
			{
				Log.error("[JOrbisComment]", e);
				break;
			}
			if (bytes<=0)
			{
				state.pcmLength = og.granulepos();
				break;
			}
			state.oy.wrote(bytes);
		}
	}

	public int write(OutputStream out)
	{
		StreamState streamout = new StreamState();
		Packet header_main = new Packet();
		Packet header_comments = new Packet();
		Packet header_codebooks = new Packet();

		Page ogout = new Page();

		Packet op = new Packet();
		long granpos = 0;

		int result;

		int index;
		byte[] buffer;

		int bytes, eosin = 0;
		int needflush = 0, needout = 0;

		header_main.bytes = state.mainlen;
		header_main.packet_base = state.mainbuf;
		header_main.packet = 0;
		header_main.b_o_s = 1;
		header_main.e_o_s = 0;
		header_main.granulepos = 0;

		header_codebooks.bytes = state.booklen;
		header_codebooks.packet_base = state.bookbuf;
		header_codebooks.packet = 0;
		header_codebooks.b_o_s = 0;
		header_codebooks.e_o_s = 0;
		header_codebooks.granulepos = 0;

		streamout.init(state.serial);

		state.vc.header_out(header_comments);

		streamout.packetin(header_main);
		streamout.packetin(header_comments);
		streamout.packetin(header_codebooks);

		while ((result = streamout.flush(ogout)) != 0)
		{
			try
			{
				out.write(ogout.header_base, ogout.header, ogout.header_len);
				out.flush();
			}
			catch (Exception e)
			{
				break;
			}
			try
			{
				out.write(ogout.body_base, ogout.body, ogout.body_len);
				out.flush();
			}
			catch (Exception e)
			{
				break;
			}
		}

		while (state.fetch_next_packet(op) != 0)
		{
			int size = state.blocksize(op);
			granpos += size;
			if (needflush != 0)
			{
				if (streamout.flush(ogout) != 0)
				{
					try
					{
						out.write(ogout.header_base, ogout.header, ogout.header_len);
						out.flush();
					}
					catch (Exception e)
					{
						Log.error("[JOrbisComment]", e);
						return -1;
					}
					try
					{
						out.write(ogout.body_base, ogout.body, ogout.body_len);
						out.flush();
					}
					catch (Exception e)
					{
						Log.error("[JOrbisComment]", e);
						return -1;
					}
				}
			}
			else if (needout != 0)
			{
				if (streamout.pageout(ogout) != 0)
				{
					try
					{
						out.write(ogout.header_base, ogout.header, ogout.header_len);
						out.flush();
					}
					catch (Exception e)
					{
						Log.error("[JOrbisComment]", e);
						return -1;
					}
					try
					{
						out.write(ogout.body_base, ogout.body, ogout.body_len);
						out.flush();
					}
					catch (Exception e)
					{
						Log.error("[JOrbisComment]", e);
						return -1;
					}
				}
			}

			needflush = needout = 0;

			if (op.granulepos == -1)
			{
				op.granulepos = granpos;
				streamout.packetin(op);
			}
			else
			{
				if (granpos > op.granulepos)
				{
					granpos = op.granulepos;
					streamout.packetin(op);
					needflush = 1;
				}
				else
				{
					streamout.packetin(op);
					needout = 1;
				}
			}
		}

		streamout.e_o_s = 1;
		while (streamout.flush(ogout) != 0)
		{
			try
			{
				out.write(ogout.header_base, ogout.header, ogout.header_len);
				out.flush();
			}
			catch (Exception e)
			{
				Log.error("[JOrbisComment]", e);
				return -1;
			}
			try
			{
				out.write(ogout.body_base, ogout.body, ogout.body_len);
				out.flush();
			}
			catch (Exception e)
			{
				Log.error("[JOrbisComment]", e);
				return -1;
			}
		}

		state.vi.clear();

		eosin = 0; /* clear it, because not all paths to here do */
		while (eosin == 0)
		{ /* We reached eos, not eof */
			/*
			 * We copy the rest of the stream (other logical streams)
			 * through, a page at a time.
			 */
			while (true)
			{
				result = state.oy.pageout(ogout);
				if (result == 0) break;
				if (result < 0)
				{
					Log.error("Corrupt or missing data, continuing...");
				}
				else
				{
					/*
					 * Don't bother going through the rest, we can just
					 * write the page out now
					 */
					try
					{
						out.write(ogout.header_base, ogout.header, ogout.header_len);
						out.flush();
					}
					catch (Exception e)
					{
						Log.error("[JOrbisComment]", e);
						return -1;
					}
					try
					{
						out.write(ogout.body_base, ogout.body, ogout.body_len);
						out.flush();
					}
					catch (Exception e)
					{
						Log.error("[JOrbisComment]", e);
						return -1;
					}
				}
			}

			index = state.oy.buffer(CHUNKSIZE);
			buffer = state.oy.data;
			try
			{
				bytes = state.in.read(buffer, index, CHUNKSIZE);
			}
			catch (Exception e)
			{
				Log.error("[JOrbisComment]", e);
				return -1;
			}
			state.oy.wrote(bytes);

			if (bytes == 0 || bytes == -1)
			{
				eosin = 1;
				break;
			}
		}

		/*
		 * cleanup:
		 * ogg_stream_clear(&streamout);
		 * ogg_packet_clear(&header_comments);
		 * free(state->mainbuf);
		 * free(state->bookbuf);
		 * jorbiscomment_clear_internals(state);
		 * if(!eosin)
		 * {
		 * state->lasterror =
		 * "Error writing stream to output. "
		 * "Output stream may be corrupted or truncated.";
		 * return -1;
		 * }
		 * return 0;
		 * }
		 */
		return 0;
	}
}
