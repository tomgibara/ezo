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
import java.util.BitSet;
import java.util.PrimitiveIterator.OfInt;

/**
 * <p>
 * Ezo is a pixel font in two weights, <i>regular</i> and <i>bold</i>, and with
 * an italic variant of each. It is a micro-font in which no character exceeds 8
 * pixels in either dimension but is nevertheless designed for readability while
 * retaining consistency, even at such small sizes. It is a proportional font
 * that includes kerning rules but features tabular digits (ie. each digit has a
 * consistent width when paired with other digits).
 *
 * <p>
 * The typeface also supports underlining via {@link #withUnderline(boolean)}
 * and adjusted word spacing via {@link #withWidthOfSpace(int)} in addition to
 * weighting via {@link #withBold(boolean)} and italicization via
 * {@link #withItalic(boolean)}.
 *
 * <p>
 * Rendering the typeface is performed by supplying a {@link Plotter} to the
 * {@link #renderer(Plotter)} method of this class to create a {@link Renderer}.
 * The {@link #renderedWidthOfString(String)} and
 * {@link #renderedWidthOfChar(int)} method may be used to compute spans prior
 * to rendering and this can be combined with measurements from
 * {@link #ascent()} and {@link #descent()} to compute simple bounding boxes for
 * text.
 *
 * <p>
 * This class can be used by multiple threads without external synchronization.
 * Passing <code>null</code> into any method of this class, or its related
 * classes will raise an <code>IllegalArgumentException</code>.
 *
 * @see #regular()
 * @see #bold()
 * @see #italic()
 * @see #boldItalic()
 *
 * @author Tom Gibara
 *
 */

public final class Ezo {

	// statics

	private static final int MIN_CHAR = 32;
	private static final int MAX_CHAR = 127;
	private static final int CHAR_COUNT = MAX_CHAR - MIN_CHAR;
	private static final int ASCENT = 6;
	private static final int DESCENT = 2;

	private static final Ezo regularEzo    = new Ezo(false, false);
	private static final Ezo boldEzo       = new Ezo(true,  false);
	private static final Ezo italicEzo     = new Ezo(false, true );
	private static final Ezo boldItalicEzo = new Ezo(true,  true );

	/**
	 * The regular weight Ezo font.
	 *
	 * @return a regular weight Ezo font
	 */

	public static final Ezo regular() { return regularEzo; }

	/**
	 * The bold weight Ezo font.
	 *
	 * @return a bold weight Ezo font
	 */

	public static final Ezo bold() { return boldEzo; }

	/**
	 * The italic Ezo font.
	 *
	 * @return the italic Ezo font
	 */

	public static final Ezo italic() { return italicEzo; }

	/**
	 * The bold weight italic Ezo font.
	 *
	 * @return the bold weight italic Ezo font
	 */

	public static final Ezo boldItalic() { return boldItalicEzo; }

	// font parameters
	private final boolean bold;
	private final boolean italic;
	private final boolean underline;
	private final int     spaceWidth;

	// font data
	private final byte[] offsets;   // distance in pixels to start of character on baseline
	private final byte[] baselines; // width in pixels of character on baseline
	private final byte[] widths;    // widths contains the width of the character in pixels
	private final byte[] classes;   // classes contains the classifications used to kern individual letter pairs.
	private final long[] bitmaps;   // bitmaps contains the the individual glyph bitmaps

	// constructor for static instances only
	private Ezo(boolean bold, boolean italic) {
		this.bold = bold;
		this.italic = italic;
		this.underline = false;
		offsets   = new byte[MAX_CHAR];
		baselines = new byte[MAX_CHAR];
		widths    = new byte[MAX_CHAR];
		classes   = new byte[MAX_CHAR];
		bitmaps   = new long[MAX_CHAR];

		String path = italic ?
				bold ? "/bold-italic.bin" : "/italic.bin" :
				bold ? "/bold.bin" : "/regular.bin";
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(Ezo.class.getResourceAsStream(path)))) {
			in.readFully(offsets,   MIN_CHAR, CHAR_COUNT);
			in.readFully(baselines, MIN_CHAR, CHAR_COUNT);
			in.readFully(widths,    MIN_CHAR, CHAR_COUNT);
			in.readFully(classes,   MIN_CHAR, CHAR_COUNT);
			for (int i = MIN_CHAR; i < MAX_CHAR; i++) {
				bitmaps[i] = in.readLong();
			}
		} catch (IOException e) {
			throw new RuntimeException("failed to load ezo data from resource " + path);
		}
		this.spaceWidth = widths[MIN_CHAR];
	}

	// constructor for derived instances
	private Ezo(boolean bold, boolean italic, boolean underline, int spaceWidth) {
		Ezo src = italic ?
				bold ? boldItalicEzo : italicEzo :
				bold ? boldEzo : regularEzo;
		this.bold = bold;
		this.italic = italic;
		this.underline = underline;
		this.offsets = src.offsets;
		this.baselines = src.baselines;
		this.widths = src.widths;
		this.classes = src.classes;
		this.bitmaps = src.bitmaps;
		this.spaceWidth = spaceWidth < 0 ? widths[MIN_CHAR] : spaceWidth;
	}

	// public constructors

	/**
	 * This style of the Ezo font the with weight as specified.
	 *
	 * @param bold
	 *            true if a bold weight font is required, false if a regular
	 *            weight font is required
	 * @return an Ezo font
	 * @see #isBold()
	 */

	public Ezo withBold(boolean bold) {
		if (bold == this.bold) return this;
		if (this == regularEzo) return boldEzo;
		if (this == boldEzo) return regularEzo;
		if (this == italicEzo) return boldItalicEzo;
		return new Ezo(bold, italic, underline, spaceWidth);
	}

	/**
	 * This style of the Ezo font with italics as specified.
	 *
	 * @param italic
	 *            true if an italic font is required, false if a non-italic font
	 *            is required
	 * @return an Ezo font
	 * @see #isItalic()
	 */

	public Ezo withItalic(boolean italic) {
		if (italic == this.italic) return this;
		if (this == regularEzo   ) return italicEzo;
		if (this == boldEzo      ) return boldItalicEzo;
		if (this == italicEzo    ) return regularEzo;
		if (this == boldItalicEzo) return boldEzo;
		return new Ezo(bold, italic, underline, spaceWidth);
	}

	public Ezo withUnderline(boolean underline) {
		return new Ezo(bold, italic, underline, spaceWidth);
	}
	/**
	 * This style of the Ezo font with the "space width" as specified.
	 *
	 * @param spaceWidth
	 *            a non-negative size in pixels
	 * @return an Ezo font
	 * @see #widthOfSpace()
	 */

	public Ezo withWidthOfSpace(int spaceWidth) {
		if (spaceWidth < 0) throw new IllegalArgumentException("negative spaceWidth");
		return this.spaceWidth == spaceWidth ? this : new Ezo(bold, italic, underline, spaceWidth);
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
	 * @see #withBold(boolean)
	 */

	public boolean isBold() {
		return bold;
	}

	/**
	 * Whether the font is italic.
	 *
	 * @return true if the font is italic, or false if not
	 * @see #withItalic(boolean)
	 */

	public boolean isItalic() {
		return italic;
	}

	/**
	 * A convenient method for identifying the width of a space. Equivalent to
	 * {@code widthOf(' ')}.
	 *
	 * @return the width of a space in this font
	 * @see #withWidthOfSpace(int)
	 */

	public int widthOfSpace() {
		return spaceWidth;
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
	 * Computes the distance advanced along the baseline when rendering of the
	 * supplied string in this font. Non-printable characters and characters not
	 * supported by the typeface are treated as having zero width.
	 *
	 * <p>
	 * Note that the total width of a string will not necessarily equal the sum
	 * of its individual characters due to kerning adjustments made between
	 * adjacent characters.
	 *
	 * <p>
	 * Note also that this is not necessarily the same as the rendered width
	 * since glyphs may project beyond the baseline.
	 *
	 * @param str
	 *            any string
	 * @return the width of the string as rendered in this font
	 * @see #renderedWidthOfString(String)
	 */

	public int baselineWidthOfString(String str) {
		if (str == null) throw new IllegalArgumentException("null str");
		OfInt cs = str.codePoints().iterator();
		int sum = 0;
		int prev = -1;
		while (cs.hasNext()) {
			int next = cs.nextInt();
			sum += delta(prev, next);
			sum += baselineWidth(next);
			prev = next;
		}
		return sum;
	}

	/**
	 * <p>
	 * Computes the width required to accommodate a rendering of the supplied
	 * string in this font. Non-printable characters and characters not
	 * supported by the typeface are treated as having zero width.
	 *
	 * <p>
	 * Note that the total width of a string will not necessarily equal the sum
	 * of its individual characters due to kerning adjustments made between
	 * adjacent characters.
	 *
	 * <p>
	 * Note also that this is not necessarily the same as the baseline width
	 * since glyphs may project beyond the baseline.
	 *
	 * @param str
	 *            any string
	 * @return the width of the string as rendered in this font
	 * @see #baselineWidthOfString(String)
	 */

	public int renderedWidthOfString(String str) {
		int bw = baselineWidthOfString(str);
		if (bw == 0) return 0;
		// ideally should use codepoints, but in practice, all surrogate-pair enchoded yield zero length at this time
		int c = str.charAt(str.length() - 1);
		return bw - baselineWidth(c) + pixelWidth(c) - offset(c);
	}

	/**
	 * <p>
	 * The distance advanced along the baseline when rendering the specified
	 * character in this font. Non-printable characters and characters that are
	 * not supported by this font are reported as having zero width.
	 *
	 * <p>
	 * In general, it is not possible to compute the width of a string by
	 * summing its individual character widths because of adjustments made for
	 * kerning; for this use {@link #baselineWidthOfString(String)}.
	 *
	 * @param c
	 *            any character
	 * @return the width the character in this font
	 */

	public int baselineWidthOfChar(int c) {
		if (c < 0) throw new IllegalArgumentException("negative c");
		return baselineWidth(c);
	}

	/**
	 * <p>
	 * The the width required to accommodate a rendering of the supplied
	 * character in this font. Non-printable characters and characters that are
	 * not supported by this font are reported as having zero width.
	 *
	 * <p>
	 * In general, it is not possible to compute the width of a string by
	 * summing its individual character widths because of adjustments made for
	 * kerning; for this use {@link #renderedWidthOfString(String)}.
	 *
	 * @param c
	 *            any character
	 * @return the width the character in this font
	 */

	public int renderedWidthOfChar(int c) {
		if (c < 0) throw new IllegalArgumentException("negative c");
		return pixelWidth(c);
	}

	/**
	 * Calculates the number of characters from a given string that will fit
	 * into a specified width when rendered in this font.
	 *
	 * @param str
	 *            the characters being measured
	 * @param width
	 *            the width of the gap into which the characters are to fit
	 * @param ellipsisWidth
	 *            the width of an ellipsis that will be displayed if the string
	 *            is truncated, zero if no ellipsis is to be displayed
	 * @return the number of characters in the string that fit ranging from zero
	 *         to the length of the string inclusive
	 */

	public int accommodatedCharCount(String str, int width, int ellipsisWidth) {
		if (str == null) throw new IllegalArgumentException("null str");
		OfInt cs = str.codePoints().iterator();
		int i = 0;
		int sum = 0;
		int prev = -1;
		while (cs.hasNext()) {
			int next = cs.nextInt();
			int delta = delta(prev, next);
			int pixelWidth = pixelWidth(next);
			int offset = offset(next);
			if (sum + delta + pixelWidth - offset > width) {
				if (ellipsisWidth == 0) return i;
				if (ellipsisWidth > width) return 0;
				return accommodatedCharCount(str, width - ellipsisWidth, 0); 
			}
			sum += delta + baselineWidth(next);
			prev = next;
			i ++;
		}
		return i;
	}

	// private utility methods

	private int pixelWidth(int c) {
		if (c == MIN_CHAR) return spaceWidth;
		if (c >= MAX_CHAR) return 0;
		return widths[c];
	}

	private int baselineWidth(int c) {
		if (c == MIN_CHAR) return spaceWidth;
		if (c >= MAX_CHAR) return 0;
		return baselines[c];
	}

	private int offset(int c) {
		return c >= MAX_CHAR ? 0 : offsets[c];
	}

	// may only be called with valid characters
	private boolean collapse(int prev, int next) {
		if (prev =='r' && next == 'n') return false; // special case: rn is too similar to m
		if (prev =='_' && next == '_') return true;  // special case: join underscores
		int prevClass = classes[prev];
		int nextClass = classes[next];
		if (prevClass == -1 || nextClass == -1) return false; // no rules apply to either
		if (prevClass <= 3) return false; // the previous character is small, so no collapse
		if (prevClass == 10 && (nextClass <= 3 || nextClass >=9)) return true; // tall characters like f can accommodate all non-big (or ligature) characters
		if (nextClass > 3) return false; // the next character is big, so no collapse
		if (prevClass > 7) return false; // curve fitting only applies to first 8 classes
		int pattern = prevClass & 3;
		return pattern == (pattern & nextClass);
	}

	private int delta(int prev, int next) {
		if (prev == -1) return 0; // don't advance on first character
		if (prev == MIN_CHAR) return 0; // don't advance further after a space
		if (baselineWidth(prev) == 0) return 0; // don't advance after non-printable character
		int delta = 1; // assume a standard space of 1 px
		if (collapse(prev, next)) delta --;
		// special cases here
		return delta;
	}

	// inner classes

	/**
	 * A plotter renders the pixels that comprise the Ezo glyphs. A plotter is
	 * necessary to render an text using Ezo. Simple implementations will simply
	 * render a pixel at the given coordinate, but alternative implementations
	 * could vary the scale, colour and geometry of the pixels.
	 */

	@FunctionalInterface
	public interface Plotter {

		/**
		 * Renders a pixel at the given coordinates.
		 *
		 * @param x the x coordinate
		 * @param y the y coordinate
		 */

		void plot(int x, int y);

	}

	/**
	 * <p>
	 * Renders characters and strings in the associated {@link Ezo} font.
	 * Instances of this class are obtained by supplying a {@link Plotter} to
	 * the {@link Ezo#renderer(Plotter)} method of {@link Ezo}.
	 *
	 * <p>
	 * Renderers record a location which is advanced each time
	 * {@link #renderChar(int)} or {@link #renderString(String)} is called. The
	 * location specifies the position of the next character to be rendered (in
	 * terms of its left-hand-side and baseline). The location of a newly
	 * created {@link Renderer} is initialized to (0,0).
	 *
	 * <p>
	 * Multi-threaded use of this class requires external synchronization
	 */

	public final class Renderer {

		private final Plotter plotter;
		private int x = 0;
		private int y = 0;

		Renderer(Plotter plotter) {
			this.plotter = plotter;
		}

		/**
		 * The Ezo font that backs this renderer.
		 *
		 * @return the ezo instance from which this renderer was created
		 */

		public Ezo ezo() {
			return Ezo.this;
		}

		/**
		 * <p>
		 * Specifies the location of the next character to be rendered by this
		 * renderer. The <code>x</code> and <code>y</code> coordinates give the
		 * left-hand=side and baseline of the character respectively. There is
		 * no prohibition on negative coordinates.
		 *
		 * <p>
		 * Calls to this method can be chained.
		 *
		 * @param x
		 *            the x coordinate of the next character
		 * @param y
		 *            the y coordinate of the next character
		 * @return this renderer
		 */

		public Renderer locate(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}

		/**
		 * The x coordinate of the next character's left-hand-side.
		 *
		 * @return the x coordinate, in pixels
		 */

		public int x() {
			return x;
		}

		/**
		 * The y coordinate of the next character's baseline.
		 *
		 * @return the y coordinate, in pixels
		 */

		public int y() {
			return y;
		}

		/**
		 * Renders the supplied string. Non-printable and unsupported characters
		 * are omitted and do not advance the location of the renderer.
		 *
		 * @param str
		 *            the string to be rendered
		 * @return the number of pixels advanced by the renderer
		 */

		public int renderString(String str) {
			if (str == null) throw new IllegalArgumentException("null str");
			OfInt cs = str.chars().iterator();
			int oldX = x;
			int prev = -1;
			BitSet line = underline ? new BitSet() : null;
			while (cs.hasNext()) {
				int next = cs.nextInt();
				renderImpl(prev, next, line, oldX - 1);
				prev = next;
			}
			if (line != null && prev != -1) renderLine(line, oldX, x - baselineWidth(prev) + pixelWidth(prev) - offset(prev));
			return x - oldX;

		}

		/**
		 * <p>
		 * Renderers the supplied character and returns the number of pixels
		 * advanced. This method cannot apply kerning rules because it renderers
		 * only one character at a time; To render characters sequentially, use
		 * the {@link #renderString(String)} method.
		 *
		 * <p>
		 * Attempting to render non-printable and unsupported characters
		 *
		 * @param c
		 *            the character to render
		 * @return the number of pixels advanced by the renderer
		 */

		public int renderChar(int c) {
			if (c < 0) throw new IllegalArgumentException();
			int oldX = x;
			BitSet line = underline ? new BitSet() : null;
			renderImpl(-1, c, line, oldX - 1);
			if (line != null) renderLine(line, oldX, pixelWidth(c) - offset(c));
			return x - oldX;
		}

		private void renderImpl(int prev, int next, BitSet line, int lineOffset) {
			int w = pixelWidth(next);
			if (w <= 0) return;
			x += delta(prev, next);
			long bits = bitmaps[next];
			int b = 0;
			int o = offset(next);
			x -= o;
			for (int py = y - ASCENT; py < y + DESCENT; py++) {
				b = (b + 7) & ~7;
				for (int px = x; px < x + w; px++, b++) {
					boolean bit = (bits << b) < 0L;
					if (!bit) continue;
					plotter.plot(px, py);
					if (line == null || py != y + 1) continue;
					line.set(px - lineOffset);
				}
			}
			x += o + baselineWidth(next);
		}

		private void renderLine(BitSet line, int from, int to) {
			int len = to - from;
			for (int px = 1; px <= len; px++) {
				boolean skip = line.get(px - 1) || line.get(px) || line.get(px + 1);
				if (!skip) plotter.plot(px + from - 1, y + DESCENT - 1);
			}
		}
	}
}
