package egovframework.cmm.util;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * 핸드폰 카메라 JPEG 등 EXIF Orientation 보정.
 * <p>{@link javax.imageio.ImageIO} 는 Orientation 을 무시하므로, PDF 변환 전에 픽셀을 회전/반전한다.
 */
public final class ImageExifOrientationUtil {

  private ImageExifOrientationUtil() {}

  /**
   * EXIF Orientation 을 읽어 이미지를 시각적으로 올바른 방향으로 보정한다.
   * Orientation 없거나 1 이면 원본 그대로 반환.
   */
  public static BufferedImage applyExifOrientation(BufferedImage image, byte[] fileBytes) {
    if (image == null) {
      return null;
    }
    int orientation = readOrientation(fileBytes);
    if (orientation <= 1 || orientation > 8) {
      return image;
    }
    return apply(image, orientation);
  }

  /**
   * JPEG APP1 Exif 의 Orientation 태그(0x0112). 없거나 파싱 실패 시 1.
   * JPEG가 아니면 1.
   */
  public static int readOrientation(byte[] data) {
    if (data == null || data.length < 4) {
      return 1;
    }
    // JPEG SOI
    if ((data[0] & 0xFF) != 0xFF || (data[1] & 0xFF) != 0xD8) {
      return 1;
    }

    int offset = 2;
    while (offset + 4 <= data.length) {
      if ((data[offset] & 0xFF) != 0xFF) {
        break;
      }
      int marker = data[offset + 1] & 0xFF;
      if (marker == 0xD9 || marker == 0xDA) {
        // EOI / SOS
        break;
      }
      if (offset + 4 > data.length) {
        break;
      }
      int segmentLength = ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);
      if (segmentLength < 2 || offset + 2 + segmentLength > data.length) {
        break;
      }

      // APP1
      if (marker == 0xE1) {
        int payloadStart = offset + 4;
        int payloadLen = segmentLength - 2;
        int orientation = readOrientationFromExifApp1(data, payloadStart, payloadLen);
        if (orientation >= 1 && orientation <= 8) {
          return orientation;
        }
      }

      offset += 2 + segmentLength;
    }
    return 1;
  }

  private static int readOrientationFromExifApp1(byte[] data, int start, int length) {
    // "Exif\0\0"
    if (length < 14) {
      return 1;
    }
    if (data[start] != 'E' || data[start + 1] != 'x' || data[start + 2] != 'i'
        || data[start + 3] != 'f' || data[start + 4] != 0 || data[start + 5] != 0) {
      return 1;
    }

    int tiffStart = start + 6;
    if (tiffStart + 8 > start + length) {
      return 1;
    }

    boolean littleEndian;
    if (data[tiffStart] == 'I' && data[tiffStart + 1] == 'I') {
      littleEndian = true;
    } else if (data[tiffStart] == 'M' && data[tiffStart + 1] == 'M') {
      littleEndian = false;
    } else {
      return 1;
    }

    int magic = readU16(data, tiffStart + 2, littleEndian);
    if (magic != 42) {
      return 1;
    }

    long ifd0Offset = readU32(data, tiffStart + 4, littleEndian);
    return readOrientationFromIfd(data, tiffStart, start + length, ifd0Offset, littleEndian);
  }

  private static int readOrientationFromIfd(byte[] data, int tiffStart, int endExclusive,
      long ifdOffset, boolean littleEndian) {
    if (ifdOffset < 0 || ifdOffset > Integer.MAX_VALUE - 2) {
      return 1;
    }
    int ifdPos = tiffStart + (int) ifdOffset;
    if (ifdPos + 2 > endExclusive) {
      return 1;
    }

    int entryCount = readU16(data, ifdPos, littleEndian);
    int entryPos = ifdPos + 2;
    for (int i = 0; i < entryCount; i++) {
      if (entryPos + 12 > endExclusive) {
        break;
      }
      int tag = readU16(data, entryPos, littleEndian);
      int type = readU16(data, entryPos + 2, littleEndian);
      // Orientation tag
      if (tag == 0x0112) {
        // SHORT(3) 또는 LONG(4). 값은 엔트리 내 오프셋 위치에 인라인.
        if (type == 3) {
          return readU16(data, entryPos + 8, littleEndian);
        }
        if (type == 4) {
          long v = readU32(data, entryPos + 8, littleEndian);
          return (int) v;
        }
        return readU16(data, entryPos + 8, littleEndian);
      }
      entryPos += 12;
    }
    return 1;
  }

  private static int readU16(byte[] data, int offset, boolean littleEndian) {
    if (offset + 2 > data.length) {
      return 0;
    }
    if (littleEndian) {
      return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
    }
    return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
  }

  private static long readU32(byte[] data, int offset, boolean littleEndian) {
    if (offset + 4 > data.length) {
      return 0;
    }
    if (littleEndian) {
      return (data[offset] & 0xFFL) | ((data[offset + 1] & 0xFFL) << 8)
          | ((data[offset + 2] & 0xFFL) << 16) | ((data[offset + 3] & 0xFFL) << 24);
    }
    return ((data[offset] & 0xFFL) << 24) | ((data[offset + 1] & 0xFFL) << 16)
        | ((data[offset + 2] & 0xFFL) << 8) | (data[offset + 3] & 0xFFL);
  }

  /**
   * EXIF Orientation(1~8)에 맞게 픽셀 변환.
   * @see <a href="https://www.impulseadventure.com/photo/exif-orientation.html">EXIF Orientation</a>
   */
  public static BufferedImage apply(BufferedImage src, int orientation) {
    if (src == null || orientation <= 1 || orientation > 8) {
      return src;
    }

    int w = src.getWidth();
    int h = src.getHeight();
    AffineTransform tx = new AffineTransform();
    int destW = w;
    int destH = h;

    switch (orientation) {
      case 2: // Flip X
        tx.scale(-1.0, 1.0);
        tx.translate(-w, 0);
        break;
      case 3: // Rotate 180
        tx.translate(w, h);
        tx.rotate(Math.PI);
        break;
      case 4: // Flip Y
        tx.scale(1.0, -1.0);
        tx.translate(0, -h);
        break;
      case 5: // Transpose (Flip X + Rotate 90 CW) ≈ Flip Y + Rotate 90 CCW
        destW = h;
        destH = w;
        tx.rotate(Math.PI / 2);
        tx.scale(1.0, -1.0);
        break;
      case 6: // Rotate 90 CW — 세로로 찍은 폰 사진에서 가장 흔함
        destW = h;
        destH = w;
        tx.translate(h, 0);
        tx.rotate(Math.PI / 2);
        break;
      case 7: // Transverse
        destW = h;
        destH = w;
        tx.scale(-1.0, 1.0);
        tx.translate(-h, 0);
        tx.translate(0, w);
        tx.rotate(3.0 * Math.PI / 2);
        break;
      case 8: // Rotate 90 CCW
        destW = h;
        destH = w;
        tx.translate(0, w);
        tx.rotate(3.0 * Math.PI / 2);
        break;
      default:
        return src;
    }

    int type = src.getType();
    if (type == BufferedImage.TYPE_CUSTOM || type == 0) {
      type = BufferedImage.TYPE_INT_RGB;
    }
    BufferedImage dest = new BufferedImage(destW, destH, type);
    Graphics2D g = dest.createGraphics();
    try {
      g.drawImage(src, tx, null);
    } finally {
      g.dispose();
    }
    return dest;
  }
}
