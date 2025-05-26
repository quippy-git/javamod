package de.quippy.jflac.metadata;

/**
 * libFLAC - Free Lossless Audio Codec library
 * Copyright (C) 2001,2002,2003  Josh Coalson
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */

import java.io.IOException;

import de.quippy.jflac.io.BitInputStream;

/**
 * VorbisComment Metadata block.
 * @author kc7bfi
 */
public class VorbisComment extends Metadata {

    //private static final int VORBIS_COMMENT_NUM_COMMENTS_LEN = 32; // bits

    private static final String EMPTY_STRING = "";
	protected byte[] vendorString = new byte[0];
    protected int numComments = 0;
    protected VorbisString[] comments;

    /**
     * The constructor.
     * @param is                The InputBitStream
     * @param length            Length of the record
     * @param isLast            True if this is the last Metadata block in the chain
     * @throws IOException      Thrown if error reading from InputBitStream
     */
    public VorbisComment(final BitInputStream is, final int length, final boolean isLast) throws IOException {
        super(isLast);

        // read vendor string
        final int len = is.readRawIntLittleEndian();
        vendorString = new byte[len];
        is.readByteBlockAlignedNoCRC(vendorString, vendorString.length);

        // read comments
        numComments = is.readRawIntLittleEndian();
        if (numComments > 0) comments = new VorbisString[numComments];
        for (int i = 0; i < numComments; i++) {
            comments[i] = new VorbisString(is);
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
	final StringBuilder sb = new StringBuilder("VendorString '").append(new String(vendorString)).append("'\n");
	sb.append("VorbisComment (count=" + numComments + ")");

        for (int i = 0; i < numComments; i++) {
            sb.append("\n\t" + comments[i].toString());
        }

        return sb.toString();

    }

    public String [] getCommentByName( final String key )  {
        if (key == null ) return null;
        final java.util.ArrayList<String> sbuff = new java.util.ArrayList<>();
        for (final VorbisString comment2 : comments)
		{
            final String comment = comment2.toString();
            final int eqpos = comment.indexOf(0x3D); //Find the equals
            if (eqpos != -1 )
                if( comment.substring(0, eqpos).equalsIgnoreCase(key) )
                    sbuff.add( comment.substring(eqpos+1, comment.length()) );
        }
        return sbuff.toArray(new String[0]);
    }
    public String getComment()
    {
		final String [] v = getCommentByName("COMMENT");
		if (v!=null && v.length>0 && v[0]!=null)
			return v[0];
		else
			return EMPTY_STRING;
    }
    public String getGenre()
    {
		final String [] v = getCommentByName("GENRE");
		if (v!=null && v.length>0 && v[0]!=null)
			return v[0];
		else
			return EMPTY_STRING;
    }
    public String getDate()
    {
		final String [] v = getCommentByName("DATE");
		if (v!=null && v.length>0 && v[0]!=null)
			return v[0];
		else
			return EMPTY_STRING;
    }
    public String getAlbum()
    {
		final String [] v = getCommentByName("ALBUM");
		if (v!=null && v.length>0 && v[0]!=null)
			return v[0];
		else
			return EMPTY_STRING;
    }
    public String getTotalDiscs()
    {
		final String [] v = getCommentByName("TOTALDISCS");
		if (v!=null && v.length>0 && v[0]!=null)
			return v[0];
		else
			return EMPTY_STRING;
    }
    public String getDiscNumber()
    {
		final String [] v = getCommentByName("DISCNUMBER");
		if (v!=null && v.length>0 && v[0]!=null)
			return v[0];
		else
			return EMPTY_STRING;
    }
    public String getTotalTracks()
    {
		final String [] v = getCommentByName("TOTALTRACKS");
		if (v!=null && v.length>0 && v[0]!=null)
			return v[0];
		else
			return EMPTY_STRING;
    }
    public String getTrackNumber()
    {
		final String [] v = getCommentByName("TRACKNUMBER");
		if (v!=null && v.length>0 && v[0]!=null)
			return v[0];
		else
			return EMPTY_STRING;
    }
    public String getTitle()
    {
		final String [] v = getCommentByName("TITLE");
		if (v!=null && v.length>0 && v[0]!=null)
			return v[0];
		else
			return EMPTY_STRING;
    }
    public String getArtist()
    {
		final String [] v = getCommentByName("ARTIST");
		if (v!=null && v.length>0 && v[0]!=null)
			return v[0];
		else
			return EMPTY_STRING;
    }
}
