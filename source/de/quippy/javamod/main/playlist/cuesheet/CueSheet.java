/*
 * @(#) CueSheet.java
 *
 * Created on 14.02.2012 by Daniel Becker
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
package de.quippy.javamod.main.playlist.cuesheet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

import de.quippy.javamod.system.Helpers;
import de.quippy.javamod.system.Log;

/**
 * @author Daniel Becker
 * This class will read a cue sheet and represent it
 */
public class CueSheet
{
	private static final String QUOTATION_MARK = "\"";

	private URL cueSheedFileName;

	private String title;
	private String performer;
	private String songwriter;
	private final ArrayList<CueFile> cueFiles;

	public CueSheet()
	{
		super();
		cueFiles = new ArrayList<>();
	}
	/**
	 * @param file
	 * @since 14.02.2012
	 */
	public CueSheet(final URL file)
	{
		this();
		cueSheedFileName = file;
		readCueSheet(file);
	}
	public CueSheet(final File file)
	{
		this(Helpers.createURLfromFile(file));
	}
	/**
	 * @param fileName
	 * @since 14.02.2012
	 */
	public CueSheet(final String file)
	{
		this(Helpers.createURLfromString(file));
	}
	/**
	 * @return the cueSheedFileName
	 * @since 14.02.2012
	 */
	public URL getCueSheedFileName()
	{
		return cueSheedFileName;
	}
	public void setCueSheedFileName(final URL fileName)
	{
		cueSheedFileName = fileName;
	}
	/**
	 * @return the title
	 * @since 14.02.2012
	 */
	public String getTitle()
	{
		return title;
	}
	/**
	 * @param title the title to set
	 * @since 14.02.2012
	 */
	public void setTitle(final String title)
	{
		this.title = title;
	}
	/**
	 * @return the performer
	 * @since 14.02.2012
	 */
	public String getPerformer()
	{
		return performer;
	}
	/**
	 * @param performer the performer to set
	 * @since 14.02.2012
	 */
	public void setPerformer(final String performer)
	{
		this.performer = performer;
	}
	/**
	 * @return the songwriter
	 * @since 14.02.2012
	 */
	public String getSongwriter()
	{
		return songwriter;
	}
	/**
	 * @param songwriter the songwriter to set
	 * @since 14.02.2012
	 */
	public void setSongwriter(final String songwriter)
	{
		this.songwriter = songwriter;
	}
	/**
	 * @return the cueFiles
	 * @since 14.02.2012
	 */
	public ArrayList<CueFile> getCueFiles()
	{
		return cueFiles;
	}
	public void addFile(final CueFile cueFile)
	{
		cueFiles.add(cueFile);
	}
	private void writeCommentBlock(final Writer writer, final String title, final String performer, final String songWriter, final int column) throws IOException
	{
		final StringBuilder columnString = new StringBuilder();
		for (int i=0; i<column; i++) columnString.append("  ");
		if (performer!=null && !performer.isEmpty()) writer.write(columnString.toString() + "PERFORMER \"" + performer + "\"\r\n");
		if (title!=null && !title.isEmpty()) writer.write(columnString.toString() + "TITLE \"" + title + "\"\r\n");
		if (songWriter!=null && !songWriter.isEmpty()) writer.write(columnString.toString() + "SONGWRITER \"" + songWriter + "\"\r\n");
	}
	public void writeCueSheet(final File toFile)
	{
		String prefix = toFile.getAbsolutePath();
		prefix = prefix.substring(0, prefix.lastIndexOf(File.separatorChar)+1).toLowerCase();
		final int prefixLen = prefix.length();

		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(toFile)));
			bw.write("REM COMMENT \""+Helpers.FULLVERSION+"\"\r\n");
			writeCommentBlock(bw, getTitle(), getPerformer(), getSongwriter(), 0);
			final ArrayList<CueFile> cueFiles = getCueFiles();
			final int filesSize = cueFiles.size();
			for (int i=0; i<filesSize; i++)
			{
				final CueFile cueFile = cueFiles.get(i);

				String fileString = Helpers.createLocalFileStringFromURL(cueFile.getFile(), true);
				if (fileString.toLowerCase().startsWith(prefix)) fileString = fileString.substring(prefixLen);

				bw.write("FILE \"" + fileString + "\" " + cueFile.getType() + "\r\n");

				final ArrayList<CueTrack> cueTracks = cueFile.getTracks();
				final int tracksSize = cueTracks.size();
				for (int j=0; j<tracksSize; j++)
				{
					final CueTrack cueTrack = cueTracks.get(j);

					final int trackNo = cueTrack.getTrackNo();
					final String track = (trackNo<10)?"0"+trackNo:String.valueOf(trackNo);
					bw.write("  TRACK "+ track +" "+ cueTrack.getFormat() + "\r\n");
					writeCommentBlock(bw, cueTrack.getTitle(), cueTrack.getPerformer(), cueTrack.getSongwriter(), 2);

					final ArrayList<CueIndex> indexes = cueTrack.getIndexes();
					final int indexSize = indexes.size();
					for (int k=0; k<indexSize; k++)
					{
						final CueIndex cueIndex = indexes.get(k);

						final int indexNo = cueIndex.getIndexNo();
						final String index = (indexNo<10)?"0"+indexNo:String.valueOf(indexNo);
						long milliIndex = cueIndex.getMillisecondIndex();
						final Integer min = Integer.valueOf((int)(milliIndex / 60000L));
						final Integer sec = Integer.valueOf((int)((milliIndex / 1000L) % 60L));
						milliIndex -= (min.longValue() * 60L + sec.longValue())*1000;
						final Integer frame = Integer.valueOf((int)(((milliIndex * 75) + 500L) / 1000L));
						final String timeIndex = String.format("%02d:%02d:%02d", min, sec, frame);
						bw.write("    INDEX "+ index +" "+ timeIndex + "\r\n");
					}
				}
			}
		}
		catch (final Throwable ex)
		{
			Log.error("[CueSheet]: Writing to \""+toFile.getAbsolutePath()+"\" failed", ex);
		}
		finally
		{
			if (bw!=null) try { bw.close(); } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
		}
	}
	public static CueSheet createCueSheet(final String file)
	{
		return new CueSheet(file);
	}
	public static CueSheet createCueSheet(final File file)
	{
		return new CueSheet(file);
	}
	public static CueSheet createCueSheet(final URL file)
	{
		return new CueSheet(file);
	}
	private static String getStringFromQuotation(final Scanner tok)
	{
		String nextToken = tok.next();

		if (nextToken.startsWith(QUOTATION_MARK))
		{
			if (nextToken.endsWith(QUOTATION_MARK))
			{
				nextToken = nextToken.substring(1, nextToken.length() - 1);
			}
			else
			{
				nextToken = nextToken.substring(1) + tok.useDelimiter(QUOTATION_MARK).next();
			}
		}
		return nextToken;
	}
	private void readCueSheet(final URL fromFile)
	{
		String line;
		BufferedReader br = null;
		Scanner tok = null;

		CueFile cueFile = null;
		CueTrack cueTrack = null;
		try
		{
			br = new BufferedReader(new InputStreamReader(fromFile.openStream(), Helpers.CODING_M3U));
			while ((line = br.readLine())!=null)
			{
				if (line.isEmpty()) continue;
				tok = new Scanner(line);
				if (!tok.hasNext()) continue;
				final String token = tok.next();

				// Skipping CATALOG, CDTEXTFILE, FLAGS, ISRC, POSTGAP, PREGAP, REM
				if (token.equalsIgnoreCase("FILE"))
				{
					final String f = CueSheet.getStringFromQuotation(tok);
					addFile(cueFile = new CueFile());
					cueFile.setFile(Helpers.createAbsolutePathForFile(fromFile, f));
					cueFile.setType(tok.next());
					cueTrack = null;
				}
				else
				if (token.equalsIgnoreCase("TRACK"))
				{
					if (cueFile==null)
					{
						tok.close();
						throw new RuntimeException("Illegal cue sheet: track without file");
					}
					cueFile.addTrack(cueTrack = new CueTrack());
					cueTrack.setTrackNo(tok.nextInt());
					cueTrack.setFormat(tok.nextLine().trim().toUpperCase());
				}
				else
				if (token.equalsIgnoreCase("TITLE"))
				{
					final String title = CueSheet.getStringFromQuotation(tok);
					if (cueTrack!=null && cueTrack.getTitle()==null)
						cueTrack.setTitle(title);
					else
						setTitle(title);
				}
				else
				if (token.equalsIgnoreCase("PERFORMER"))
				{
					final String performer = CueSheet.getStringFromQuotation(tok);
					if (cueTrack!=null && cueTrack.getPerformer()==null)
						cueTrack.setPerformer(performer);
					else
						setPerformer(performer);
				}
				else
				if (token.equalsIgnoreCase("SONGWRITER"))
				{
					final String songWriter = CueSheet.getStringFromQuotation(tok);
					if (cueTrack!=null && cueTrack.getSongwriter()==null)
						cueTrack.setSongwriter(songWriter);
					else
						setSongwriter(songWriter);
				}
				else
				if (token.equalsIgnoreCase("INDEX"))
				{
					if (cueTrack==null)
					{
						tok.close();
						throw new RuntimeException("Illegal cue sheet: index without track");
					}
					final int indexNo = tok.nextInt();
					final Scanner sc = tok.useDelimiter(Pattern.compile(":| "));
					final int minutes = sc.nextInt(), seconds = sc.nextInt(), frames = sc.nextInt();
					final CueIndex index = new CueIndex();
					index.setIndexNo(indexNo);
					index.setMillisecondIndex(((minutes*60L+seconds)*1000L) + (frames * 1000L / 75L));
					cueTrack.addIndex(index);
				}
			}
		}
		catch (final Throwable ex)
		{
			Log.error("[CueSheet]: Loading failed", ex);
		}
		finally
		{
			if (br!=null) try { br.close(); } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
			if (tok!=null) tok.close();
		}
	}
}
