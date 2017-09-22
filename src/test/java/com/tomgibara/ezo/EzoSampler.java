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
			"IJKLMNOPQ",
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
		int height = 512;
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, width - 1, height - 1);
		renderSample(g, 6, false);
		g.translate(width / 2, 0);
		renderSample(g, 0, true);
		g.dispose();
		ImageIO.write(img, "PNG", new File("ezo_sampler.png"));
	}

	private static void renderSample(Graphics2D g, int inset, boolean bold) throws IOException {
		int s = 3;
		Renderer bigRenderer = Ezo.bold(bold).renderer((x,y) -> g.fillRect(x*s, y*s, s, s));
		writeLines(bigRenderer, inset, 12, 10, bigLines);
		Renderer smallRenderer = Ezo.bold(bold).renderer((x,y) -> g.fillRect(x, y, 1, 1));
		writeLines(smallRenderer, inset * s, bigRenderer.y() * s + 26, 10, smallLines);
	}

	private static void writeLines(Renderer renderer, int x, int y, int s, String... lines) {
		for (int i = 0; i < lines.length; i++) {
			renderer.locate(x, y + i * s).renderString(lines[i]);
		}
	}
}
