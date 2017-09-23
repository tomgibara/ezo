package com.tomgibara.ezo;

import java.util.Arrays;

public class EzoUnicode {

	private static final char[] lookup = {
			' ', '▘', '▝', '▀',
			'▖', '▌', '▞', '▛',
			'▗', '▚', '▐', '▜',
			'▄', '▙', '▟', '█',
	};
	
	public static void main(String... args) {
		String text = "Ezo - a Java renderered pixel font";
		Ezo ezo = Ezo.bold();
		int width = (ezo.widthOfString(text) + 2) / 2;
		int height = (ezo.ascent() + ezo.descent() + 2) / 2;
		char[] chars = new char[width * height];
		ezo.renderer((x, y) -> {
			int cx = x / 2;
			int cy = y / 2;
			int dx = x & 1;
			int dy = y & 1;
			int i = (dy << 1) + dx;
			chars[cy * width + cx] |= 1 << i;
		}).locate(1, ezo.ascent() + 1).renderString(text);
		for (int i = 0; i < chars.length; i++) {
			chars[i] = lookup[chars[i]];
		}
		for (int y = 0; y < height; y++) {
			System.out.println(Arrays.copyOfRange(chars, y * width, y * width + width));
		}
	}
}
