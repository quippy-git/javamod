/*
 * @(#) FastMath.java
 * Created on 29.01.2012 by Daniel Becker
 * -----------------------------------------------------------------------
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
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

/**
 * This class provides fast mathematical calculations which are good
 * enough for some audio calculations.
 * 
 * Results from a benchmark:
 * === sin ===
 * max abs error: 0.002056233678349173
 * 
 * === cos ===
 * max abs error: 0.0020562337006925224
 * 
 * === exp ===
 * max relative error: 0.016748421865565175
 * 
 * === log ===
 * max relative error: 0.16600066001181757
 * 
 * === pow ===
 * max abs error: 4.355155840791321E7
 * max relative error: 0.023296620585072942
 * 
 * === ultra fast pow === (yes, it's fast, but mostly wrong. Do not use it)
 * max abs error: 7.554160640000019E8
 * max relative error: 0.2391985954837864
 * 
 * === sqrt ===
 * max relative error: 4.716988791170354E-6
 * 
 * === inv sqrt ===
 * max relative error: 4.697479434203409E-6
 *  
 * @author Daniel Becker
 * @since 29.01.2012
 */
public class FastMath
{
	private static final double B = 1.2732395447351628D;
	private static final double C = -0.4052847345693511D;
	private static final double P = 0.218D;
	private static final double hPI = Math.PI / 2D;
	private static final double PI2 = Math.PI * 2D;
	private static final double atan2_coeff_1 = 0.78539816339744828D;
	private static final double atan2_coeff_2 = 2.3561944901923448D;

	public FastMath()
	{
	}

	/**
	 * Sin and Cos both rely on this internal implementation
	 * 
	 * @since 29.01.2023
	 * @param theta
	 * @return
	 */
	private static double fastSin0(final double theta)
	{
		double y = B * theta + C * theta * Math.abs(theta);
		y = P * (y * Math.abs(y) - y) + y;
		return y;
	}
	/**
	 * Wrap the π
	 * 
	 * @since 29.01.2023
	 * @param angle
	 * @return
	 */
	private static double wrap(double angle)
	{
		angle %= PI2;
		if (angle > Math.PI)
			angle -= PI2;
		else if (angle < -Math.PI) angle += PI2;

		if (angle < -Math.PI || angle > Math.PI)
			throw new IllegalArgumentException("Wrong angel : " + angle);
		else
			return angle;
	}
	/**
	 * valid only in [-π, π] after normalization
	 * 
	 * @since 29.01.2023
	 * @param theta
	 * @return
	 */
	public static double fastSin(final double theta)
	{
		return fastSin0(wrap(theta));
	}
	/**
	 * valid only in [-π, π] after normalization
	 * 
	 * @since 29.01.2023
	 * @param theta
	 * @return
	 */
	public static double fastCos(final double theta)
	{
		return fastSin0(wrap(theta + hPI));
	}
	/**
	 * @since 29.01.2023
	 * @param y
	 * @param x
	 * @return
	 */
	public static double atan2(final double y, final double x)
	{
		if (y == 0.0D) return 0.0D;
		if (x == 0.0D) return (y > 0.0D) ? hPI : -hPI;

		final double abs_y = Math.abs(y);
		double angle;
		if (x > 0.0D)
		{
			final double r = (x - abs_y) / (x + abs_y);
			angle = atan2_coeff_1 - atan2_coeff_1 * r;
		}
		else
		{
			final double r = (x + abs_y) / (abs_y - x);
			angle = atan2_coeff_2 - atan2_coeff_1 * r;
		}
		return y >= 0.0D ? angle : -angle;
	}
	/**
	 * @since 29.01.2023
	 * @param x
	 * @return
	 */
	public static double fastExp(final double x)
	{
	    if (x > 709.0) return Double.POSITIVE_INFINITY;
	    if (x < -745.0) return 0.0;

	    // range reduction: x = k*ln2 + r
	    final double fx = x * 1.4426950408889634;
	    final int k = (int) fx;

	    final double r = x - k * 0.6931471805599453;

	    // polynomial exp approximation on small interval
	    final double r2 = r * r;

	    double y = 1.0
	             + r
	             + 0.5 * r2
	             + 0.16666666666666666 * r2 * r;

	    // reconstruct exponent
	    return Double.longBitsToDouble(((long)(k + 1023)) << 52) * y;
	}
	/**
	 * @since 01.06.2026
	 * @param bits
	 * @return
	 */
	public static int log2(final int bits)
	{
	    if (bits <= 0) throw new IllegalArgumentException("No negative numbers at Log2 allowed!");
	    return 31 - Integer.numberOfLeadingZeros(bits);
	}
	/**
	 * @since 29.01.2023
	 * @param x
	 * @return
	 */
	public static double fastLog(final double x)
	{
		final long bits = Double.doubleToRawLongBits(x);

		final int exp = (int) ((bits >> 52) & 0x7FF) - 1023;
		long mantBits = (bits & ((1L << 52) - 1));

		// normalized mantissa in [1,2)
		double m = 1.0 + (mantBits / (double) (1L << 52));

		// range reduction improves accuracy
		m = (m - 1.0) / (m + 1.0);

		final double m2 = m * m;

		// 2nd-order log approximation (better than pure Taylor)
		final double ln_m = 2.0 * (m + (m2 * m) / 3.0);

		return exp * 0.6931471805599453 + ln_m;
	}
	/**
	 * @since 29.01.2023
	 * @param a
	 * @param b
	 * @return
	 */
	public static double fastPow(final double a, final double b)
	{
		if (a <= 0) return Double.NaN;

		return fastExp(b * fastLog(a));
	}
	/**
	 * Faster than fastPow, but only approximation / heuristic. You decide!
	 * "I am very fast in math"
	 * "OK, what is 12*8"
	 * "102"
	 * "That is wrong!"
	 * "But I was fast!"
	 * Kept it because of the interesting bit shifting with "magic numbers"
	 * 
	 * @since 29.01.2023
	 * @param a
	 * @param b
	 * @return
	 */
	public static double ultraFastPow(final double a, final double b)
	{
		final long x = Double.doubleToRawLongBits(a) >> 32;
		final long y = (long) (b * (x - 1072632447L) + 1072632447L);
		return Double.longBitsToDouble(y << 32);
	}
	/**
	 * @since 29.01.2023
	 * @param value
	 * @return
	 */
	public static float floor(final float value)
	{
		int result = (int) value;
		return result - (value < result ? 1 : 0);
	}
	/**
	 * @since 29.01.2023
	 * @param value
	 * @return
	 */
	public static double floor(final double value)
	{
		long result = (long) value;
		return result - (value < result ? 1 : 0);
	}
	/**
	 * A java port of the 1/sqrt(x) float version
	 * used in Quake III
	 * 
	 * @since 29.01.2023
	 * @param number
	 * @return
	 */
	public static float fastInvSqrt(final float number)
	{
		if (!Float.isFinite(number) || number <= 0F) return Float.NaN; // not defined

		final float xhalf = 0.5F * number;

		int i = Float.floatToRawIntBits(number); // C-Code equivalent: int i = *(int *)&y;
		i = 0x5f3759df - (i >> 1); // Magic Number for Float
		float result = Float.intBitsToFloat(i); // C-Code equivalent: float result = *(float *)&i;

		// 1–2 Newton steps
		result *= 1.5F - (xhalf * result * result);
		result *= 1.5F - (xhalf * result * result); // optional second iteration for improved precision

		return result;
	}
	/**
	 * Using the Quake InvSQRT to calculate sqrt
	 * 
	 * @since 29.01.2023
	 * @param x
	 * @return
	 */
	public static float fastSqrt(final float x)
	{
		return x * fastInvSqrt(x);
	}
	/**
	 * A java port of the 1/sqrt(x) double version
	 * used in Quake III
	 * 
	 * @since 29.01.2023
	 * @param number
	 * @return
	 */
	public static double fastInvSqrt(final double number)
	{
		if (!Double.isFinite(number) || number <= 0D) return Double.NaN; // not defined

		final double xhalf = 0.5d * number;

		long i = Double.doubleToRawLongBits(number); // C-Code equivalent: long i = *(long *)&y;
		i = 0x5fe6ec85e7de30daL - (i >> 1); // Magic Number for Double
		double result = Double.longBitsToDouble(i); // C-Code equivalent: double result = *(double *)&i;

		// 1–2 Newton steps
		result *= 1.5D - (xhalf * result * result);
		result *= 1.5D - (xhalf * result * result); // optional second iteration for improved precision

		return result;
	}
	/**
	 * Using the Quake InvSQRT to calculate sqrt
	 * 
	 * @since 29.01.2023
	 * @param x
	 * @return
	 */
	public static double fastSqrt(final double x)
	{
		return x * fastInvSqrt(x);
	}
}
