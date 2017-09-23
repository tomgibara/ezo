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
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class EzoDisco {

	private static final Color[] colors = {
			new Color(0xffde7725),
			new Color(0xff1d9113),
			new Color(0xffcb371e),
			new Color(0xffbc10ab),
	};

	public static void main(String... args) throws IOException {
		String text = "Disco";
		int scale = 12;
		Ezo ezo = Ezo.bold();
		int width = ezo.widthOfString(text);
		int height = ezo.ascent() + ezo.descent();
		BufferedImage image = new BufferedImage(scale * (width + 2), scale * (height + 2), TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		ezo.renderer((x, y) -> {
			int c = (x + y) / 3;
			g.setColor(colors[c & 3]);
			g.fillOval(x * scale + 1, y * scale + 1, scale - 2, scale -2);
			}).locate(1, ezo.ascent() + 1).renderString(text);
		g.dispose();
		ImageIO.write(image, "PNG", new File("ezo_disco.png"));
	}
}
