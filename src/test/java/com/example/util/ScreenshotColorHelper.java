package com.example.util;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class ScreenshotColorHelper {

    private final RemoteWebDriver driver;

    public ScreenshotColorHelper(RemoteWebDriver driver) {
        this.driver = driver;
    }

    /**
     * Returnerar medelfärgen av icke-vita pixlar inom elementets yta.
     */
    public Color averageColorOf(WebElement element) throws Exception {
        byte[] bytes = driver.getScreenshotAs(OutputType.BYTES);
        BufferedImage screenshot = ImageIO.read(new ByteArrayInputStream(bytes));

        Point location = element.getLocation();
        Dimension size = element.getSize();
        int x0 = Math.max(0, location.getX());
        int y0 = Math.max(0, location.getY());
        int x1 = Math.min(screenshot.getWidth(),  x0 + size.getWidth());
        int y1 = Math.min(screenshot.getHeight(), y0 + size.getHeight());

        long rSum = 0, gSum = 0, bSum = 0, count = 0;
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                Color px = new Color(screenshot.getRGB(x, y));
                if (px.getRed() < 240 || px.getGreen() < 240 || px.getBlue() < 240) {
                    rSum += px.getRed();
                    gSum += px.getGreen();
                    bSum += px.getBlue();
                    count++;
                }
            }
        }

        if (count == 0) {
            throw new AssertionError("Hittade inga färgade pixlar i elementets yta");
        }
        return new Color((int)(rSum / count), (int)(gSum / count), (int)(bSum / count));
    }

    /**
     * Kontrollerar att elementets medelfärg matchar förväntad hexfärg inom given tolerans.
     */
    public boolean matchesColor(WebElement element, String hexColor, int tolerance) throws Exception {
        Color actual   = averageColorOf(element);
        Color expected = Color.decode(hexColor);
        return Math.abs(actual.getRed()   - expected.getRed())   <= tolerance &&
               Math.abs(actual.getGreen() - expected.getGreen()) <= tolerance &&
               Math.abs(actual.getBlue()  - expected.getBlue())  <= tolerance;
    }

    public String describeColor(WebElement element) throws Exception {
        Color c = averageColorOf(element);
        return String.format("RGB(%d,%d,%d)", c.getRed(), c.getGreen(), c.getBlue());
    }
}
