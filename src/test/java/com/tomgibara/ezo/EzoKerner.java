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
import com.tomgibara.ezo.Ezo.Renderer;

public class EzoKerner {

	public static void main(String... args) throws IOException {
		checkKern(false, false);
		checkKern(true,  false);
		checkKern(false, true );
		checkKern(true,  true );
	}

	private static void checkKern(boolean bold, boolean italic) throws IOException {
		int space = 16;
		int width = space * 52;
		int height = space * 52;
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.BLACK);
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		Renderer renderer = Ezo.regular().withBold(bold).withItalic(italic).renderer((x,y) -> g.fillRect(x, y, 1, 1));
		for (int y = 0; y < chars.length(); y++) {
			for (int x = 0; x < chars.length(); x++) {
				String str = "" + chars.charAt(y) + chars.charAt(x);
				renderer.locate(x * space, y * space + 12).renderString(str);
			}
		}
		g.dispose();
		String name = italic ?
				bold ? "ezo_kerning_bold_italic.png" : "ezo_kerning_italic.png" :
				bold ? "ezo_kerning_bold.png" : "ezo_kerning_regular.png";
		ImageIO.write(img, "PNG", new File(name));
	}
}
