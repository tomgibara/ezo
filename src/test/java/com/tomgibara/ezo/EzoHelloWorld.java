package com.tomgibara.ezo;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.tomgibara.ezo.Ezo;
import com.tomgibara.ezo.Ezo.Renderer;

public class EzoHelloWorld {

	public static void main(String... args) throws IOException {
		// choose some text to render
		String text = "Hello, World!";
		// a scale for the rendering to improve visibility
		int scale = 8;
		// get an instance of the font, bold() is also an option
		Ezo ezo = Ezo.regular();
		// find out how large the text is
		int width = ezo.widthOfString(text);
		int height = ezo.ascent() + ezo.descent();
		// create a suitably sized image
		BufferedImage image = new BufferedImage(scale * (width + 2), scale * (height + 2), TYPE_INT_RGB);
		// create a graphics context
		Graphics2D g = image.createGraphics();
		// blank the image
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		// prepare the graphics context
		g.setColor(Color.BLACK);
		g.scale(scale, scale);
		// now fluently render the text
		ezo.renderer((x, y) -> g.fillRect(x, y, 1, 1)).locate(1, ezo.ascent() + 1).renderString(text);
		// dispose of the graphics
		g.dispose();
		// save the result
		ImageIO.write(image, "PNG", new File("ezo_hello_world.png"));
	}
	
}
