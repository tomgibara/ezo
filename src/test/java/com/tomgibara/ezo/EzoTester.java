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

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.tomgibara.ezo.Ezo;
import com.tomgibara.ezo.Ezo.Plotter;

public class EzoTester {

	public static void main(String... args) throws IOException {
		// collect arguments
		if (args.length < 2) {
			System.out.println("Usage: text filename");
			System.exit(1);
		}
		String text = args[0];
		String filename = args[1];

		// calculate image size based on the 'widest' font instance
		int scale = 8;
		Ezo ezo = Ezo.boldItalic();
		// find out how large the text is
		int width = ezo.renderedWidthOfString(text);
		int ascent = ezo.ascent();
		int descent = ezo.descent();
		int lineHeight = ascent + descent + 1;
		int height = lineHeight * 8;

		// prepare the Java graphics
		BufferedImage image = new BufferedImage(scale * (width + 2), scale * (height + 1), TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		g.setColor(Color.BLACK);
		g.scale(scale, scale);

		// render the text
		Plotter plotter = (x, y) -> g.fillRect(x, y, 1, 1);
		Ezo.regular()                       .renderer(plotter).locate(1,                + ascent + 1).renderString(text);
		Ezo.bold()                          .renderer(plotter).locate(1, lineHeight     + ascent + 1).renderString(text);
		Ezo.italic()                        .renderer(plotter).locate(1, lineHeight * 2 + ascent + 1).renderString(text);
		Ezo.boldItalic()                    .renderer(plotter).locate(1, lineHeight * 3 + ascent + 1).renderString(text);
		Ezo.regular()   .withUnderline(true).renderer(plotter).locate(1, lineHeight * 4 + ascent + 1).renderString(text);
		Ezo.bold()      .withUnderline(true).renderer(plotter).locate(1, lineHeight * 5 + ascent + 1).renderString(text);
		Ezo.italic()    .withUnderline(true).renderer(plotter).locate(1, lineHeight * 6 + ascent + 1).renderString(text);
		Ezo.boldItalic().withUnderline(true).renderer(plotter).locate(1, lineHeight * 7 + ascent + 1).renderString(text);

		// finish-up
		g.dispose();
		ImageIO.write(image, "PNG", new File(filename));
	}
	
}
