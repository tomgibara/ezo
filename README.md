# Ezo
A small self-contained pixel font.

## Features

 * An extremely small font for low density displays.
 * Two font weights: **bold** and regular.
 * Open, and consistent character shapes for readability.
 * Kerning rules to maximize character density.
 * Covers the printable ASCII character set.
 * A small library with no dependencies.
 * Custom plotting allows any mode of rendering to be supported.
 * A fluent, simple API.

## Sample

![Sampler](https://github.com/tomgibara/ezo/wiki/images/ezo_sampler.png)

More samples are available in the associated [github wiki][0].

## Example

```java
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
BufferedImage image = new BufferedImage(
   scale * (width + 2),  // width
   scale * (height + 2), // height
   TYPE_INT_RGB          // type
   );
// create a graphics context
Graphics2D g = image.createGraphics();
// blank the image
g.setColor(Color.WHITE);
g.fillRect(0, 0, image.getWidth(), image.getHeight());
// prepare the graphics context
g.setColor(Color.BLACK);
g.scale(scale, scale);
// now fluently render the text
ezo.renderer((x, y) -> g.fillRect(x, y, 1, 1))
   .locate(1, ezo.ascent() + 1)
   .renderString(text);
// dispose of the graphics
g.dispose();
// save the result
ImageIO.write(image, "PNG", new File("ezo_hello_world.png"));
```
## Usage

The ezo library is currently in development. A future version will be available
from the Maven central repository:

> Group ID:    `com.tomgibara.ezo`
> Artifact ID: `ezo`
> Version:     `1.0.0`

The Maven dependency being:

    <dependency>
      <groupId>com.tomgibara.ezo</groupId>
      <artifactId>ezo</artifactId>
      <version>1.0.0</version>
    </dependency>

## Release History

*This library has not yet been released*

[0]: https://github.com/tomgibara/ezo/wiki
