package com.mossflower.vod_service.util;

import cn.hutool.core.img.ImgUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * @author z's'b
 * 获取图片颜色工具类
 */
public class ColorUtil {

    private static final int COLOR_AVG_VAL = 51;

    public static String getThemeColor(URL url) {
        BufferedImage bi = ImgUtil.toBufferedImage(ImgUtil.scale(ImgUtil.read(url), 0.3f));
        int w = bi.getWidth();
        int h = bi.getHeight();
        float[] dots = new float[]{0.15f, 0.35f, 0.5f, 0.7f, 0.85f};
        int r = 0;
        int g = 0;
        int b = 0;
        for (float dw : dots) {
            for (float dh : dots) {
                int rgbVal = bi.getRGB((int) (w * dw), (int) (h * dh));
                Color color = ImgUtil.getColor(rgbVal);
                r += color.getRed();
                g += color.getGreen();
                b += color.getBlue();
            }
        }
        int cn = dots.length * dots.length;
        Color color = new Color(r / cn, g / cn, b / cn);
        r = Math.round(color.getRed() / COLOR_AVG_VAL);
        g = Math.round(color.getGreen() / COLOR_AVG_VAL);
        b = Math.round(color.getBlue() / COLOR_AVG_VAL);
        return String.format("#%02X%02X%02X", r, g, b);
    }

}
