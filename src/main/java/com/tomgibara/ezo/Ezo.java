/*
 * Copyright 2017 Tom Gibara
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.tomgibara.ezo;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.PrimitiveIterator.OfInt;

/**
 * <p>
 * Ezo is a pixel font in two weights, <i>regular</i> and <i>bold</i>. It is a
 * micro-font in which no character exceeds 7 pixels in either dimension but is
 * designed for readability while retaining consistency, even at such small
 * sizes. It is a proportional font that includes kerning rules but features
 * tabular digits (ie. each digit has a consistent width when paired with other
 * digits).
 *
 * <p>
 * Rendering the typeface is performed by supplying a {@link Plotter} to the
 * {@link #renderer(Plotter)} method of this class to create a {@link Renderer}.
 * The {@link #widthOfString(String)} and {@link #widthOfChar(int)} method may
 * be used to compute spans prior to rendering and this can be combined with
 * measurements from {@link #ascent()} and {@link #descent()} to compute simple
 * bounding boxes for text.
 *
 * <p>
 * This class can be used by multiple threads without external synchronization.
 * Passing <code>null</code> into any method of this class, or its related
 * classes and interfaces will raise an <code>IllegalArgumentException</code>.
 *
 * @see #regular()
 * @see #bold()
 *
 * @author Tom Gibara
 *
 */

public final class Ezo {

	// statics

	private static final int MIN_CHAR = 32;
	private static final int MAX_CHAR = 127;
	private static final int ASCENT = 6;
	private static final int DESCENT = 2;

	private static final Ezo regularPix = new Ezo(false);
	private static final Ezo boldPix = new Ezo(true);

	/**
	 * The regular weight Ezo font.
	 *
	 * @return a regular weight Ezo font.
	 */

	public static final Ezo regular() { return regularPix; }

	/**
	 * The bold weight Ezo font.
	 *
	 * @return a bold weight Ezo font.
	 */

	public static final Ezo bold() { return boldPix; }

	/**
	 * An instance of the Ezo font.
	 *
	 * @param bold
	 *            true if a bold weight font is required, false if a regular
	 *            weight font is required
	 * @return an Ezo font
	 */

	public static final Ezo bold(boolean bold) {
		return bold ? boldPix : regularPix;
	}

	private final boolean bold;

	// widths contains the width of the character in pixels, this includes a one pixel space.
	private final byte[] widths = new byte[MAX_CHAR];

	// classes contains the class of a character which is used to kern individual letter pairs.
	private final byte[] classes = new byte[MAX_CHAR];

	// bitmaps contains the the individual glyph bitmaps
	private final long[] bitmaps = new long[MAX_CHAR];

	private Ezo(boolean bold) {
		this.bold = bold;
		String path = bold ? "/bold.bin" : "/regular.bin";
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(Ezo.class.getResourceAsStream(path)))) {
			in.readFully(widths, MIN_CHAR, MAX_CHAR - MIN_CHAR);
			in.readFully(classes, MIN_CHAR, MAX_CHAR - MIN_CHAR);
			for (int i = MIN_CHAR; i < MAX_CHAR; i++) {
				bitmaps[i] = in.readLong();
			}
		} catch (IOException e) {
			throw new RuntimeException("failed to load ezo data from resource " + path);
		}
	}

	// public accessors

	/**
	 * The maximum number of pixels that any character in the typeface extends
	 * above the baseline.
	 *
	 * @return the ascent of the font.
	 */

	public int ascent() {
		return ASCENT;
	}

	/**
	 * The maximum number of pixels that any character in the typeface extends
	 * above the baseline.
	 *
	 * @return the ascent of the font.
	 */

	public int descent() {
		return DESCENT;
	}

	/**
	 * Whether the weight of the font is bold, or else regular.
	 *
	 * @return true if the font is bold, or false if the fond has regular
	 *         weight.
	 */

	public boolean isBold() {
		return bold;
	}

	// public methods

	/**
	 * Creates a new renderer that can draw strings and characters in this font.
	 *
	 * @param plotter
	 *            an object used to plot the pixels with which characters are
	 *            composed
	 * @return a renderer for this font.
	 */

	public Renderer renderer(Plotter plotter) {
		if (plotter == null) throw new IllegalArgumentException("null plotter");
		return new Renderer(plotter);
	}

	/**
	 * <p>
	 * Computes the width of the supplied string in this font. Non-printable
	 * characters and characters not supported by the typeface are treated as
	 * having zero width.
	 *
	 * <p>
	 * Note that the total width of a string will not necessarily equal the sum
	 * of its individual characters due to kerning adjustments made between
	 * adjacent characters.
	 *
	 * @param str
	 *            any string
	 * @return the width of the string as rendered in this font
	 */

	public int widthOfString(String str) {
		if (str == null) throw new IllegalArgumentException("null str");
		OfInt cs = str.codePoints().iterator();
		int sum = 0;
		int prev = -1;
		while (cs.hasNext()) {
			int next = cs.nextInt();
			int w = width(next);
			if (w > 0) {
				sum += w;
				if (prev != -1 && collapse(prev, next)) sum--;
			}
			prev = next;
		}
		return sum == 0 ? 0 : sum - 1;
	}

	/**
	 * <p>
	 * The width of the given character in this font. Non-printable characters
	 * and characters that are not supported by this font are reported as having
	 * zero width.
	 *
	 * <p>
	 * In general, it is not possible to compute the width of a string by
	 * summing its individual character widths because of adjustments made for
	 * kerning; for this use {@link #widthOfString(String)}.
	 *
	 * @param c
	 *            any character
	 * @return the width the character in this font
	 */

	public int widthOfChar(int c) {
		if (c < 0) throw new IllegalArgumentException("negative c");
		int w = width(c);
		return w == 0 ? 0 : w - 1;
	}

	// private utility methods

	private int width(int c) {
		return c >= MAX_CHAR ? 0 : widths[c];
	}

	// may only be called with valid characters
	private boolean collapse(int prev, int next) {
		if (prev =='r' && next == 'n') return false; // special case: rn is too similar to m
		if (prev =='_' && next == '_') return true;  // special case: join underscores
		int prevClass = classes[prev];
		int nextClass = classes[next];
		if (nextClass == 11) return true; // underhangs can always be collapsed
		if (prevClass == -1 || nextClass == -1) return false; // no rules apply to either
		if (prevClass <= 3) return false; // the previous character is small, so no collapse
		if (prevClass == 10 && (nextClass <= 3 || nextClass >=9)) return true; // tall characters like f can accommodate all non-big (or ligature) characters
		if (nextClass > 3) return false; // the next character is big, so no collapse
		if (prevClass > 7) return false; // curve fitting only applies to first 8 classes
		int pattern = prevClass & 3;
		return pattern == (pattern & nextClass);
	}

	// inner classes

	@FunctionalInterface
	public interface Plotter {

		void plot(int x, int y);

	}

	public final class Renderer {

		private final Plotter plotter;
		private int x = 0;
		private int y = 0;

		Renderer(Plotter plotter) {
			this.plotter = plotter;
		}

		public Renderer locate(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}

		public int x() {
			return x;
		}

		public int y() {
			return y;
		}

		public int renderString(String str) {
			if (str == null) throw new IllegalArgumentException("null str");
			OfInt cs = str.chars().iterator();
			int oldX = x;
			int prev = -1;
			while (cs.hasNext()) {
				int next = cs.nextInt();
				renderImpl(prev, next);
				prev = next;
			}
			return x - oldX;

		}

		public int renderChar(int c) {
			if (c < 0) throw new IllegalArgumentException();
			int oldX = x;
			renderImpl(-1, c);
			return x - oldX;
		}

		private void renderImpl(int prev, int next) {
			int w = width(next);
			if (w == -1) return;
			boolean collapse = prev != -1 && widthOfChar(prev) != 0 && collapse(prev, next);
			if (collapse) x --;
			long bits = bitmaps[next];
			int b = 0;
			for (int py = y - ASCENT; py < y + DESCENT; py++) {
				b = (b + 7) & ~7;
				for (int px = x; px < x + w; px++, b++) {
					if ((bits << b) < 0L) plotter.plot(px, py);
				}
			}
			x += w;
		}

	}
}
