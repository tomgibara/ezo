# Ezo
A small self-contained pixel font.

## Features

 * An extremely small font for low density displays.
 * Two font weights: **bold** and regular.
 * *Italic* styles for both weights.
 * Option to underline text.
 * Adjustable inter-word spacing.
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
String text  = ... // the text to render
Graphics2D g = ... // a graphics context to render to
int originX  = ... // x-coordinate of first character
int originY  = ... // y-coordinate of text baseline

Ezo.regular();                                 // get a font instance
   .renderer((x, y) -> g.fillRect(x, y, 1, 1)) // define a plotter
   .locate(originX, originY)                   // position the text
   .renderString(text);                        // render the text
```
A complete [*"Hello, World!"* example][1], with documentation, is available
amongst [other sample code][2].

## Usage

The ezo library is available from the Maven central repository:

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

**tbc** Version 2.0.0

 * Improved Q letter shape.
 * Corrected vertical position of bold underscore.
 * Added italic versions of bold and regular fonts.
 * Added option to change inter-word spacing.
 * Added option to render characters and strings with underlines.
 * Introduced distinction between "baseline width" and "rendered width" in API.


**2017.09.28** Version 1.0.0

 Initial release

[0]: https://github.com/tomgibara/ezo/wiki
[1]: https://github.com/tomgibara/ezo/blob/master/src/test/java/com/tomgibara/ezo/EzoHelloWorld.java
[2]: https://github.com/tomgibara/ezo/blob/master/src/test/java/com/tomgibara/ezo/
