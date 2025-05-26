/*
 * @(#) StringUtils.java
 * Created on 11.04.2023 by Daniel Becker
 * -----------------------------------------------------------------------
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * ----------------------------------------------------------------------
 */
package de.quippy.javamod.system;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Becker
 * @since 11.04.2023
 */
public class StringUtils
{
	private static final HashMap<Character, String> htmlEncodeChars = new HashMap<>();

	static
	{
		// Special characters for HTML
		htmlEncodeChars.put(Character.valueOf('\u0026'), "&amp;");
		htmlEncodeChars.put(Character.valueOf('\u003C'), "&lt;");
		htmlEncodeChars.put(Character.valueOf('\u003E'), "&gt;");
		htmlEncodeChars.put(Character.valueOf('\u0022'), "&quot;");

		htmlEncodeChars.put(Character.valueOf('\u0152'), "&OElig;");
		htmlEncodeChars.put(Character.valueOf('\u0153'), "&oelig;");
		htmlEncodeChars.put(Character.valueOf('\u0160'), "&Scaron;");
		htmlEncodeChars.put(Character.valueOf('\u0161'), "&scaron;");
		htmlEncodeChars.put(Character.valueOf('\u0178'), "&Yuml;");
		htmlEncodeChars.put(Character.valueOf('\u02C6'), "&circ;");
		htmlEncodeChars.put(Character.valueOf('\u02DC'), "&tilde;");
		htmlEncodeChars.put(Character.valueOf('\u2002'), "&ensp;");
		htmlEncodeChars.put(Character.valueOf('\u2003'), "&emsp;");
		htmlEncodeChars.put(Character.valueOf('\u2009'), "&thinsp;");
		htmlEncodeChars.put(Character.valueOf('\u200C'), "&zwnj;");
		htmlEncodeChars.put(Character.valueOf('\u200D'), "&zwj;");
		htmlEncodeChars.put(Character.valueOf('\u200E'), "&lrm;");
		htmlEncodeChars.put(Character.valueOf('\u200F'), "&rlm;");
		htmlEncodeChars.put(Character.valueOf('\u2013'), "&ndash;");
		htmlEncodeChars.put(Character.valueOf('\u2014'), "&mdash;");
		htmlEncodeChars.put(Character.valueOf('\u2018'), "&lsquo;");
		htmlEncodeChars.put(Character.valueOf('\u2019'), "&rsquo;");
		htmlEncodeChars.put(Character.valueOf('\u201A'), "&sbquo;");
		htmlEncodeChars.put(Character.valueOf('\u201C'), "&ldquo;");
		htmlEncodeChars.put(Character.valueOf('\u201D'), "&rdquo;");
		htmlEncodeChars.put(Character.valueOf('\u201E'), "&bdquo;");
		htmlEncodeChars.put(Character.valueOf('\u2020'), "&dagger;");
		htmlEncodeChars.put(Character.valueOf('\u2021'), "&Dagger;");
		htmlEncodeChars.put(Character.valueOf('\u2030'), "&permil;");
		htmlEncodeChars.put(Character.valueOf('\u2039'), "&lsaquo;");
		htmlEncodeChars.put(Character.valueOf('\u203A'), "&rsaquo;");
		htmlEncodeChars.put(Character.valueOf('\u20AC'), "&euro;");

		// Character entity references for ISO 8859-1 characters
		htmlEncodeChars.put(Character.valueOf('\u00A0'), "&nbsp;");
		htmlEncodeChars.put(Character.valueOf('\u00A1'), "&iexcl;");
		htmlEncodeChars.put(Character.valueOf('\u00A2'), "&cent;");
		htmlEncodeChars.put(Character.valueOf('\u00A3'), "&pound;");
		htmlEncodeChars.put(Character.valueOf('\u00A4'), "&curren;");
		htmlEncodeChars.put(Character.valueOf('\u00A5'), "&yen;");
		htmlEncodeChars.put(Character.valueOf('\u00A6'), "&brvbar;");
		htmlEncodeChars.put(Character.valueOf('\u00A7'), "&sect;");
		htmlEncodeChars.put(Character.valueOf('\u00A8'), "&uml;");
		htmlEncodeChars.put(Character.valueOf('\u00A9'), "&copy;");
		htmlEncodeChars.put(Character.valueOf('\u00AA'), "&ordf;");
		htmlEncodeChars.put(Character.valueOf('\u00AB'), "&laquo;");
		htmlEncodeChars.put(Character.valueOf('\u00AC'), "&not;");
		htmlEncodeChars.put(Character.valueOf('\u00AD'), "&shy;");
		htmlEncodeChars.put(Character.valueOf('\u00AE'), "&reg;");
		htmlEncodeChars.put(Character.valueOf('\u00AF'), "&macr;");
		htmlEncodeChars.put(Character.valueOf('\u00B0'), "&deg;");
		htmlEncodeChars.put(Character.valueOf('\u00B1'), "&plusmn;");
		htmlEncodeChars.put(Character.valueOf('\u00B2'), "&sup2;");
		htmlEncodeChars.put(Character.valueOf('\u00B3'), "&sup3;");
		htmlEncodeChars.put(Character.valueOf('\u00B4'), "&acute;");
		htmlEncodeChars.put(Character.valueOf('\u00B5'), "&micro;");
		htmlEncodeChars.put(Character.valueOf('\u00B6'), "&para;");
		htmlEncodeChars.put(Character.valueOf('\u00B7'), "&middot;");
		htmlEncodeChars.put(Character.valueOf('\u00B8'), "&cedil;");
		htmlEncodeChars.put(Character.valueOf('\u00B9'), "&sup1;");
		htmlEncodeChars.put(Character.valueOf('\u00BA'), "&ordm;");
		htmlEncodeChars.put(Character.valueOf('\u00BB'), "&raquo;");
		htmlEncodeChars.put(Character.valueOf('\u00BC'), "&frac14;");
		htmlEncodeChars.put(Character.valueOf('\u00BD'), "&frac12;");
		htmlEncodeChars.put(Character.valueOf('\u00BE'), "&frac34;");
		htmlEncodeChars.put(Character.valueOf('\u00BF'), "&iquest;");
		htmlEncodeChars.put(Character.valueOf('\u00C0'), "&Agrave;");
		htmlEncodeChars.put(Character.valueOf('\u00C1'), "&Aacute;");
		htmlEncodeChars.put(Character.valueOf('\u00C2'), "&Acirc;");
		htmlEncodeChars.put(Character.valueOf('\u00C3'), "&Atilde;");
		htmlEncodeChars.put(Character.valueOf('\u00C4'), "&Auml;");
		htmlEncodeChars.put(Character.valueOf('\u00C5'), "&Aring;");
		htmlEncodeChars.put(Character.valueOf('\u00C6'), "&AElig;");
		htmlEncodeChars.put(Character.valueOf('\u00C7'), "&Ccedil;");
		htmlEncodeChars.put(Character.valueOf('\u00C8'), "&Egrave;");
		htmlEncodeChars.put(Character.valueOf('\u00C9'), "&Eacute;");
		htmlEncodeChars.put(Character.valueOf('\u00CA'), "&Ecirc;");
		htmlEncodeChars.put(Character.valueOf('\u00CB'), "&Euml;");
		htmlEncodeChars.put(Character.valueOf('\u00CC'), "&Igrave;");
		htmlEncodeChars.put(Character.valueOf('\u00CD'), "&Iacute;");
		htmlEncodeChars.put(Character.valueOf('\u00CE'), "&Icirc;");
		htmlEncodeChars.put(Character.valueOf('\u00CF'), "&Iuml;");
		htmlEncodeChars.put(Character.valueOf('\u00D0'), "&ETH;");
		htmlEncodeChars.put(Character.valueOf('\u00D1'), "&Ntilde;");
		htmlEncodeChars.put(Character.valueOf('\u00D2'), "&Ograve;");
		htmlEncodeChars.put(Character.valueOf('\u00D3'), "&Oacute;");
		htmlEncodeChars.put(Character.valueOf('\u00D4'), "&Ocirc;");
		htmlEncodeChars.put(Character.valueOf('\u00D5'), "&Otilde;");
		htmlEncodeChars.put(Character.valueOf('\u00D6'), "&Ouml;");
		htmlEncodeChars.put(Character.valueOf('\u00D7'), "&times;");
		htmlEncodeChars.put(Character.valueOf('\u00D8'), "&Oslash;");
		htmlEncodeChars.put(Character.valueOf('\u00D9'), "&Ugrave;");
		htmlEncodeChars.put(Character.valueOf('\u00DA'), "&Uacute;");
		htmlEncodeChars.put(Character.valueOf('\u00DB'), "&Ucirc;");
		htmlEncodeChars.put(Character.valueOf('\u00DC'), "&Uuml;");
		htmlEncodeChars.put(Character.valueOf('\u00DD'), "&Yacute;");
		htmlEncodeChars.put(Character.valueOf('\u00DE'), "&THORN;");
		htmlEncodeChars.put(Character.valueOf('\u00DF'), "&szlig;");
		htmlEncodeChars.put(Character.valueOf('\u00E0'), "&agrave;");
		htmlEncodeChars.put(Character.valueOf('\u00E1'), "&aacute;");
		htmlEncodeChars.put(Character.valueOf('\u00E2'), "&acirc;");
		htmlEncodeChars.put(Character.valueOf('\u00E3'), "&atilde;");
		htmlEncodeChars.put(Character.valueOf('\u00E4'), "&auml;");
		htmlEncodeChars.put(Character.valueOf('\u00E5'), "&aring;");
		htmlEncodeChars.put(Character.valueOf('\u00E6'), "&aelig;");
		htmlEncodeChars.put(Character.valueOf('\u00E7'), "&ccedil;");
		htmlEncodeChars.put(Character.valueOf('\u00E8'), "&egrave;");
		htmlEncodeChars.put(Character.valueOf('\u00E9'), "&eacute;");
		htmlEncodeChars.put(Character.valueOf('\u00EA'), "&ecirc;");
		htmlEncodeChars.put(Character.valueOf('\u00EB'), "&euml;");
		htmlEncodeChars.put(Character.valueOf('\u00EC'), "&igrave;");
		htmlEncodeChars.put(Character.valueOf('\u00ED'), "&iacute;");
		htmlEncodeChars.put(Character.valueOf('\u00EE'), "&icirc;");
		htmlEncodeChars.put(Character.valueOf('\u00EF'), "&iuml;");
		htmlEncodeChars.put(Character.valueOf('\u00F0'), "&eth;");
		htmlEncodeChars.put(Character.valueOf('\u00F1'), "&ntilde;");
		htmlEncodeChars.put(Character.valueOf('\u00F2'), "&ograve;");
		htmlEncodeChars.put(Character.valueOf('\u00F3'), "&oacute;");
		htmlEncodeChars.put(Character.valueOf('\u00F4'), "&ocirc;");
		htmlEncodeChars.put(Character.valueOf('\u00F5'), "&otilde;");
		htmlEncodeChars.put(Character.valueOf('\u00F6'), "&ouml;");
		htmlEncodeChars.put(Character.valueOf('\u00F7'), "&divide;");
		htmlEncodeChars.put(Character.valueOf('\u00F8'), "&oslash;");
		htmlEncodeChars.put(Character.valueOf('\u00F9'), "&ugrave;");
		htmlEncodeChars.put(Character.valueOf('\u00FA'), "&uacute;");
		htmlEncodeChars.put(Character.valueOf('\u00FB'), "&ucirc;");
		htmlEncodeChars.put(Character.valueOf('\u00FC'), "&uuml;");
		htmlEncodeChars.put(Character.valueOf('\u00FD'), "&yacute;");
		htmlEncodeChars.put(Character.valueOf('\u00FE'), "&thorn;");
		htmlEncodeChars.put(Character.valueOf('\u00FF'), "&yuml;");

		// Mathematical, Greek and Symbolic characters for HTML
		htmlEncodeChars.put(Character.valueOf('\u0192'), "&fnof;");
		htmlEncodeChars.put(Character.valueOf('\u0391'), "&Alpha;");
		htmlEncodeChars.put(Character.valueOf('\u0392'), "&Beta;");
		htmlEncodeChars.put(Character.valueOf('\u0393'), "&Gamma;");
		htmlEncodeChars.put(Character.valueOf('\u0394'), "&Delta;");
		htmlEncodeChars.put(Character.valueOf('\u0395'), "&Epsilon;");
		htmlEncodeChars.put(Character.valueOf('\u0396'), "&Zeta;");
		htmlEncodeChars.put(Character.valueOf('\u0397'), "&Eta;");
		htmlEncodeChars.put(Character.valueOf('\u0398'), "&Theta;");
		htmlEncodeChars.put(Character.valueOf('\u0399'), "&Iota;");
		htmlEncodeChars.put(Character.valueOf('\u039A'), "&Kappa;");
		htmlEncodeChars.put(Character.valueOf('\u039B'), "&Lambda;");
		htmlEncodeChars.put(Character.valueOf('\u039C'), "&Mu;");
		htmlEncodeChars.put(Character.valueOf('\u039D'), "&Nu;");
		htmlEncodeChars.put(Character.valueOf('\u039E'), "&Xi;");
		htmlEncodeChars.put(Character.valueOf('\u039F'), "&Omicron;");
		htmlEncodeChars.put(Character.valueOf('\u03A0'), "&Pi;");
		htmlEncodeChars.put(Character.valueOf('\u03A1'), "&Rho;");
		htmlEncodeChars.put(Character.valueOf('\u03A3'), "&Sigma;");
		htmlEncodeChars.put(Character.valueOf('\u03A4'), "&Tau;");
		htmlEncodeChars.put(Character.valueOf('\u03A5'), "&Upsilon;");
		htmlEncodeChars.put(Character.valueOf('\u03A6'), "&Phi;");
		htmlEncodeChars.put(Character.valueOf('\u03A7'), "&Chi;");
		htmlEncodeChars.put(Character.valueOf('\u03A8'), "&Psi;");
		htmlEncodeChars.put(Character.valueOf('\u03A9'), "&Omega;");
		htmlEncodeChars.put(Character.valueOf('\u03B1'), "&alpha;");
		htmlEncodeChars.put(Character.valueOf('\u03B2'), "&beta;");
		htmlEncodeChars.put(Character.valueOf('\u03B3'), "&gamma;");
		htmlEncodeChars.put(Character.valueOf('\u03B4'), "&delta;");
		htmlEncodeChars.put(Character.valueOf('\u03B5'), "&epsilon;");
		htmlEncodeChars.put(Character.valueOf('\u03B6'), "&zeta;");
		htmlEncodeChars.put(Character.valueOf('\u03B7'), "&eta;");
		htmlEncodeChars.put(Character.valueOf('\u03B8'), "&theta;");
		htmlEncodeChars.put(Character.valueOf('\u03B9'), "&iota;");
		htmlEncodeChars.put(Character.valueOf('\u03BA'), "&kappa;");
		htmlEncodeChars.put(Character.valueOf('\u03BB'), "&lambda;");
		htmlEncodeChars.put(Character.valueOf('\u03BC'), "&mu;");
		htmlEncodeChars.put(Character.valueOf('\u03BD'), "&nu;");
		htmlEncodeChars.put(Character.valueOf('\u03BE'), "&xi;");
		htmlEncodeChars.put(Character.valueOf('\u03BF'), "&omicron;");
		htmlEncodeChars.put(Character.valueOf('\u03C0'), "&pi;");
		htmlEncodeChars.put(Character.valueOf('\u03C1'), "&rho;");
		htmlEncodeChars.put(Character.valueOf('\u03C2'), "&sigmaf;");
		htmlEncodeChars.put(Character.valueOf('\u03C3'), "&sigma;");
		htmlEncodeChars.put(Character.valueOf('\u03C4'), "&tau;");
		htmlEncodeChars.put(Character.valueOf('\u03C5'), "&upsilon;");
		htmlEncodeChars.put(Character.valueOf('\u03C6'), "&phi;");
		htmlEncodeChars.put(Character.valueOf('\u03C7'), "&chi;");
		htmlEncodeChars.put(Character.valueOf('\u03C8'), "&psi;");
		htmlEncodeChars.put(Character.valueOf('\u03C9'), "&omega;");
		htmlEncodeChars.put(Character.valueOf('\u03D1'), "&thetasym;");
		htmlEncodeChars.put(Character.valueOf('\u03D2'), "&upsih;");
		htmlEncodeChars.put(Character.valueOf('\u03D6'), "&piv;");
		htmlEncodeChars.put(Character.valueOf('\u2022'), "&bull;");
		htmlEncodeChars.put(Character.valueOf('\u2026'), "&hellip;");
		htmlEncodeChars.put(Character.valueOf('\u2032'), "&prime;");
		htmlEncodeChars.put(Character.valueOf('\u2033'), "&Prime;");
		htmlEncodeChars.put(Character.valueOf('\u203E'), "&oline;");
		htmlEncodeChars.put(Character.valueOf('\u2044'), "&frasl;");
		htmlEncodeChars.put(Character.valueOf('\u2118'), "&weierp;");
		htmlEncodeChars.put(Character.valueOf('\u2111'), "&image;");
		htmlEncodeChars.put(Character.valueOf('\u211C'), "&real;");
		htmlEncodeChars.put(Character.valueOf('\u2122'), "&trade;");
		htmlEncodeChars.put(Character.valueOf('\u2135'), "&alefsym;");
		htmlEncodeChars.put(Character.valueOf('\u2190'), "&larr;");
		htmlEncodeChars.put(Character.valueOf('\u2191'), "&uarr;");
		htmlEncodeChars.put(Character.valueOf('\u2192'), "&rarr;");
		htmlEncodeChars.put(Character.valueOf('\u2193'), "&darr;");
		htmlEncodeChars.put(Character.valueOf('\u2194'), "&harr;");
		htmlEncodeChars.put(Character.valueOf('\u21B5'), "&crarr;");
		htmlEncodeChars.put(Character.valueOf('\u21D0'), "&lArr;");
		htmlEncodeChars.put(Character.valueOf('\u21D1'), "&uArr;");
		htmlEncodeChars.put(Character.valueOf('\u21D2'), "&rArr;");
		htmlEncodeChars.put(Character.valueOf('\u21D3'), "&dArr;");
		htmlEncodeChars.put(Character.valueOf('\u21D4'), "&hArr;");
		htmlEncodeChars.put(Character.valueOf('\u2200'), "&forall;");
		htmlEncodeChars.put(Character.valueOf('\u2202'), "&part;");
		htmlEncodeChars.put(Character.valueOf('\u2203'), "&exist;");
		htmlEncodeChars.put(Character.valueOf('\u2205'), "&empty;");
		htmlEncodeChars.put(Character.valueOf('\u2207'), "&nabla;");
		htmlEncodeChars.put(Character.valueOf('\u2208'), "&isin;");
		htmlEncodeChars.put(Character.valueOf('\u2209'), "&notin;");
		htmlEncodeChars.put(Character.valueOf('\u220B'), "&ni;");
		htmlEncodeChars.put(Character.valueOf('\u220F'), "&prod;");
		htmlEncodeChars.put(Character.valueOf('\u2211'), "&sum;");
		htmlEncodeChars.put(Character.valueOf('\u2212'), "&minus;");
		htmlEncodeChars.put(Character.valueOf('\u2217'), "&lowast;");
		htmlEncodeChars.put(Character.valueOf('\u221A'), "&radic;");
		htmlEncodeChars.put(Character.valueOf('\u221D'), "&prop;");
		htmlEncodeChars.put(Character.valueOf('\u221E'), "&infin;");
		htmlEncodeChars.put(Character.valueOf('\u2220'), "&ang;");
		htmlEncodeChars.put(Character.valueOf('\u2227'), "&and;");
		htmlEncodeChars.put(Character.valueOf('\u2228'), "&or;");
		htmlEncodeChars.put(Character.valueOf('\u2229'), "&cap;");
		htmlEncodeChars.put(Character.valueOf('\u222A'), "&cup;");
		htmlEncodeChars.put(Character.valueOf('\u222B'), "&int;");
		htmlEncodeChars.put(Character.valueOf('\u2234'), "&there4;");
		htmlEncodeChars.put(Character.valueOf('\u223C'), "&sim;");
		htmlEncodeChars.put(Character.valueOf('\u2245'), "&cong;");
		htmlEncodeChars.put(Character.valueOf('\u2248'), "&asymp;");
		htmlEncodeChars.put(Character.valueOf('\u2260'), "&ne;");
		htmlEncodeChars.put(Character.valueOf('\u2261'), "&equiv;");
		htmlEncodeChars.put(Character.valueOf('\u2264'), "&le;");
		htmlEncodeChars.put(Character.valueOf('\u2265'), "&ge;");
		htmlEncodeChars.put(Character.valueOf('\u2282'), "&sub;");
		htmlEncodeChars.put(Character.valueOf('\u2283'), "&sup;");
		htmlEncodeChars.put(Character.valueOf('\u2284'), "&nsub;");
		htmlEncodeChars.put(Character.valueOf('\u2286'), "&sube;");
		htmlEncodeChars.put(Character.valueOf('\u2287'), "&supe;");
		htmlEncodeChars.put(Character.valueOf('\u2295'), "&oplus;");
		htmlEncodeChars.put(Character.valueOf('\u2297'), "&otimes;");
		htmlEncodeChars.put(Character.valueOf('\u22A5'), "&perp;");
		htmlEncodeChars.put(Character.valueOf('\u22C5'), "&sdot;");
		htmlEncodeChars.put(Character.valueOf('\u2308'), "&lceil;");
		htmlEncodeChars.put(Character.valueOf('\u2309'), "&rceil;");
		htmlEncodeChars.put(Character.valueOf('\u230A'), "&lfloor;");
		htmlEncodeChars.put(Character.valueOf('\u230B'), "&rfloor;");
		htmlEncodeChars.put(Character.valueOf('\u2329'), "&lang;");
		htmlEncodeChars.put(Character.valueOf('\u232A'), "&rang;");
		htmlEncodeChars.put(Character.valueOf('\u25CA'), "&loz;");
		htmlEncodeChars.put(Character.valueOf('\u2660'), "&spades;");
		htmlEncodeChars.put(Character.valueOf('\u2663'), "&clubs;");
		htmlEncodeChars.put(Character.valueOf('\u2665'), "&hearts;");
		htmlEncodeChars.put(Character.valueOf('\u2666'), "&diams;");
	}

	/**
	 * Constructor for StringUtils
	 */
	public StringUtils()
	{
		super();
	}

	/**
	 * @since 11.04.2023
	 * @param source
	 * @return
	 */
	public static String encodeHtml(final String source)
	{
		return encode(source, htmlEncodeChars);
	}

	/**
	 * @since 11.04.2023
	 * @param source
	 * @param encodingTable
	 * @return
	 */
	private static String encode(final String source, final Map<Character, String> encodingTable)
	{
		if (source == null || encodingTable == null)
		{
			return source;
		}

		StringBuilder encoded_string = null;
		final char[] string_to_encode_array = source.toCharArray();
		int last_match = -1;
		int difference = 0;

		for (int i=0; i<string_to_encode_array.length; i++)
		{
			final Character char_to_encode = Character.valueOf(string_to_encode_array[i]);

			if (encodingTable.containsKey(char_to_encode))
			{
				// lazy... But if nothing is to replace, avoid object creation and return source string
				if (encoded_string == null) encoded_string = new StringBuilder(source.length());

				difference = i - (last_match + 1);
				if (difference>0) encoded_string.append(string_to_encode_array, last_match + 1, difference);
				encoded_string.append(encodingTable.get(char_to_encode));
				last_match = i;
			}
		}

		if (encoded_string == null) return source;

		difference = string_to_encode_array.length - (last_match + 1);
		if (difference>0)
			encoded_string.append(string_to_encode_array, last_match + 1, difference);
		return encoded_string.toString();
	}
}
