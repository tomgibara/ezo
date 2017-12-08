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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.tomgibara.ezo.Ezo;
import com.tomgibara.ezo.Ezo.Plotter;
import com.tomgibara.ezo.Ezo.Renderer;

public class EzoSampler {

	private static final String[] bigLines = new String[] {
			"ABCDEFGHI",
			"JKLMNOPQ",
			"RSTUVWXYZ",
			"abcdefghijklmn",
			"opqrstuvwxyz",
			"01234567890",
			"!\"#$%&'()*+",
			",-./:;<=>?@",
			"[\\]^_`{|}~",
	};

	private static final String[] smallLines = new String[] {
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ",
			"abcdefghijklmnopqrstuvwxyz",
			"01234567890 !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~",
			"",
			"Sphinx of black quartz, judge my vow.",
			"The baffled fish flowed off the office desk.",
			"",
			"\"Reading is sometimes an ingenious device",
			"for avoiding thought.\"",
			"",
			"$+123,434.53  Tabular",
			"$-112,083.20  numerals",
			"",
			"[{(< >)}]   @user #tag   hot & cold   on/off",
			"",
			"All human beings are born free and equal",
			"in dignity and rights. They are endowed",
			"with reason and conscience and should",
			"act towards one another in a spirit of",
			"brotherhood.",
	};

	public static void main(String... args) throws IOException {
		int width = 512;
		int height = 1024;
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, width - 1, height - 1);
		renderSample(g, 6, false, false);
		g.translate(width / 2, 0);
		renderSample(g, 0, true, false);
		g.translate(0, height / 2);
		renderSample(g, 0, true, true);
		g.translate(-width / 2, 0);
		renderSample(g, 6, false, true);
		g.dispose();
		ImageIO.write(img, "PNG", new File("ezo_sampler.png"));
	}

	private static void renderSample(Graphics2D g, int inset, boolean bold, boolean italic) throws IOException {
		int s = 3;
		Ezo ezo = Ezo.regular().withBold(bold).withItalic(italic);
		Renderer bigRenderer = ezo.renderer((x,y) -> g.fillRect(x*s, y*s, s, s));
		writeLines(bigRenderer, inset, 12, 10, bigLines);
		Renderer smallRenderer = ezo.renderer((x,y) -> g.fillRect(x, y, 1, 1));
		writeLines(smallRenderer, inset * s, bigRenderer.y() * s + 26, 10, smallLines);
	}

	private static void writeLines(Renderer renderer, int x, int y, int s, String... lines) {
		for (int i = 0; i < lines.length; i++) {
			renderer.locate(x, y + i * s).renderString(lines[i]);
		}
	}
}
