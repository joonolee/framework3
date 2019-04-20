package framework.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import nl.captcha.Captcha;
import nl.captcha.gimpy.RippleGimpyRenderer;
import nl.captcha.servlet.CaptchaServletUtil;

/**
 * 이미지 포맷 변경, 크기 변경시 이용할 수 있는 유틸리티 클래스이다.
 */
public final class ImageUtil {

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private ImageUtil() {
	}

	/**
	 * 이미지를 리사이즈 한다.
	 * 소스 이미지 파일의 width, height 중 크기가 큰 쪽을 기준으로 하여 비율을 유지한채 이미지를 생성한다.
	 * @param srcPath 소스 이미지 경로
	 * @param destPath 대상 이미지 경로
	 * @param width 리사이즈할 가로 사이즈
	 * @param height 리사이즈할 세로 사이즈
	 */
	public static void resize(String srcPath, String destPath, int width, int height) {
		File srcFile = new File(srcPath);
		File destFile = new File(destPath);
		resize(srcFile, destFile, width, height);
	}

	/**
	 * 이미지를 리사이즈 한다.(jpg로 저장)
	 * 소스 이미지 파일의 width, height 중 크기가 큰 쪽을 기준으로 하여 비율을 유지한채 이미지를 생성한다.
	 * @param srcFile 소스 이미지 파일
	 * @param destFile 대상 이미지 파일
	 * @param width 리사이즈할 가로 사이즈
	 * @param height 리사이즈할 세로 사이즈
	 */
	public static void resize(File srcFile, File destFile, int width, int height) {
		Image resizedImg = null;
		BufferedImage bufImg = null;
		try {
			BufferedImage image = ImageIO.read(srcFile);
			double scale = getScale(width, height, image.getWidth(), image.getHeight());
			int scaleWidth = (int) (scale * image.getWidth());
			int scaleHeight = (int) (scale * image.getHeight());
			resizedImg = image.getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_SMOOTH);
			bufImg = new BufferedImage(resizedImg.getWidth(null), resizedImg.getHeight(null), image.getType());
			Graphics2D g2d = bufImg.createGraphics();
			g2d.drawImage(resizedImg, 0, 0, null);
			g2d.dispose();
			ImageIO.write(bufImg, "jpg", destFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (resizedImg != null) {
				resizedImg.flush();
			}
			if (bufImg != null) {
				bufImg.flush();
			}
		}
	}

	/**
	 * 이미지를 리사이즈 한다.(jpg로 저장)
	 * 소스 이미지 파일의 width를 기준으로 하여 비율을 유지한채 이미지를 생성한다.
	 * @param srcPath 소스 이미지 경로
	 * @param destPath 대상 이미지 경로
	 * @param width 리사이즈할 가로 사이즈
	 */
	public static void resizeWidth(String srcPath, String destPath, int width) {
		File srcFile = new File(srcPath);
		File destFile = new File(destPath);
		resizeWidth(srcFile, destFile, width);
	}

	/**
	 * 이미지를 리사이즈 한다.(jpg로 저장)
	 * 소스 이미지 파일의 width를 기준으로 하여 비율을 유지한채 이미지를 생성한다.
	 * @param srcFile 소스 이미지 파일
	 * @param destFile 대상 이미지 파일
	 * @param width 리사이즈할 가로 사이즈
	 */
	public static void resizeWidth(File srcFile, File destFile, int width) {
		Image resizedImg = null;
		BufferedImage bufImg = null;
		try {
			BufferedImage image = ImageIO.read(srcFile);
			double scale = getScale(width, image.getWidth());
			int scaleWidth = (int) (scale * image.getWidth());
			int scaleHeight = (int) (scale * image.getHeight());
			resizedImg = image.getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_SMOOTH);
			bufImg = new BufferedImage(resizedImg.getWidth(null), resizedImg.getHeight(null), image.getType());
			Graphics2D g2d = bufImg.createGraphics();
			g2d.drawImage(resizedImg, 0, 0, null);
			g2d.dispose();
			ImageIO.write(bufImg, "jpg", destFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (resizedImg != null) {
				resizedImg.flush();
			}
			if (bufImg != null) {
				bufImg.flush();
			}
		}
	}

	/**
	 * 이미지를 리사이즈 한다.(jpg로 저장)
	 * 소스 이미지 파일의 height를 기준으로 하여 비율을 유지한채 이미지를 생성한다.
	 * @param srcPath 소스 이미지 경로
	 * @param destPath 대상 이미지 경로
	 * @param height 리사이즈할 세로 사이즈
	 */
	public static void resizeHeight(String srcPath, String destPath, int height) {
		File srcFile = new File(srcPath);
		File destFile = new File(destPath);
		resizeHeight(srcFile, destFile, height);
	}

	/**
	 * 이미지를 리사이즈 한다.(jpg로 저장)
	 * 소스 이미지 파일의 height를 기준으로 하여 비율을 유지한채 이미지를 생성한다.
	 * @param srcFile 소스 이미지 파일
	 * @param destFile 대상 이미지 파일
	 * @param height 리사이즈할 세로 사이즈
	 */
	public static void resizeHeight(File srcFile, File destFile, int height) {
		Image resizedImg = null;
		BufferedImage bufImg = null;
		try {
			BufferedImage image = ImageIO.read(srcFile);
			double scale = getScale(height, image.getHeight());
			int scaleWidth = (int) (scale * image.getWidth());
			int scaleHeight = (int) (scale * image.getHeight());
			resizedImg = image.getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_SMOOTH);
			bufImg = new BufferedImage(resizedImg.getWidth(null), resizedImg.getHeight(null), image.getType());
			Graphics2D g2d = bufImg.createGraphics();
			g2d.drawImage(resizedImg, 0, 0, null);
			g2d.dispose();
			ImageIO.write(bufImg, "jpg", destFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (resizedImg != null) {
				resizedImg.flush();
			}
			if (bufImg != null) {
				bufImg.flush();
			}
		}
	}

	/**
	 * 이미지를 오른쪽으로 90도 회전한다.(jpg로 저장)
	 * 소스 이미지 파일의 크기는 유지한채 이미지를 오른쪽으로 90도 회전한다.
	 * @param srcPath 소스 이미지 경로
	 * @param destPath 대상 이미지 경로
	 */
	public static void rotate90(String srcPath, String destPath) {
		File srcFile = new File(srcPath);
		File destFile = new File(destPath);
		rotate90(srcFile, destFile);
	}

	/**
	 * 이미지를 오른쪽으로 90도 회전한다.(jpg로 저장)
	 * 소스 이미지 파일의 크기는 유지한채 이미지를 오른쪽으로 90도 회전한다.
	 * @param srcFile 소스 이미지 파일
	 * @param destFile 대상 이미지 파일
	 */
	public static void rotate90(File srcFile, File destFile) {
		rotate(srcFile, destFile, 90);
	}

	/**
	 * 이미지를 180도 회전한다.(jpg로 저장)
	 * 소스 이미지 파일의 크기는 유지한채 이미지를 180도 회전한다.
	 * @param srcPath 소스 이미지 경로
	 * @param destPath 대상 이미지 경로
	 */
	public static void rotate180(String srcPath, String destPath) {
		File srcFile = new File(srcPath);
		File destFile = new File(destPath);
		rotate180(srcFile, destFile);
	}

	/**
	 * 이미지를 180도 회전한다.(jpg로 저장)
	 * 소스 이미지 파일의 크기는 유지한채 이미지를 180도 회전한다.
	 * @param srcFile 소스 이미지 파일
	 * @param destFile 대상 이미지 파일
	 */
	public static void rotate180(File srcFile, File destFile) {
		rotate(srcFile, destFile, 180);
	}

	/**
	 * 이미지를 270도(왼쪽으로 90도) 회전한다.(jpg로 저장)
	 * 소스 이미지 파일의 크기는 유지한채 이미지를 270도(왼쪽으로 90도) 회전한다.
	 * @param srcPath 소스 이미지 경로
	 * @param destPath 대상 이미지 경로
	 */
	public static void rotate270(String srcPath, String destPath) {
		File srcFile = new File(srcPath);
		File destFile = new File(destPath);
		rotate270(srcFile, destFile);
	}

	/**
	 * 이미지를 270도(왼쪽으로 90도) 회전한다.(jpg로 저장)
	 * 소스 이미지 파일의 크기는 유지한채 이미지를 270도(왼쪽으로 90도) 회전한다.
	 * @param srcFile 소스 이미지 파일
	 * @param destFile 대상 이미지 파일
	 */
	public static void rotate270(File srcFile, File destFile) {
		rotate(srcFile, destFile, 270);
	}

	/**
	 * CAPTCHA 이미지를 응답객체로 전송하고, 생성된 문자열을 리턴한다.
	 * 기본사이즈는 가로 200px, 세로 50px으로 한다.
	 * @param response captcha 이미지를 전송할 응답객체
	 * @return 생성된 문자열
	 */
	public static String captcha(HttpServletResponse response) {
		return captcha(response, 200, 50);
	}

	/**
	 * CAPTCHA 이미지를 응답객체로 전송하고, 생성된 문자열을 리턴한다.
	 * @param response captcha 이미지를 전송할 응답객체
	 * @param width 가로 사이즈 픽셀
	 * @param height 세로 사이즈 픽셀
	 * @return 생성된 문자열
	 */
	public static String captcha(HttpServletResponse response, int width, int height) {
		response.reset();
		Captcha captcha = new Captcha.Builder(width, height).addText().addBackground().gimp(new RippleGimpyRenderer()).build();
		CaptchaServletUtil.writeImage(response, captcha.getImage());
		return captcha.getAnswer();
	}

	/**
	 * QRCode 이미지를 생성한다.
	 * @param url QRCode 스캔 시 이동할 곳의 URL
	 * @param destPath QRCode 파일명
	 * @param width QRCode 이미지 가로 길이
	 */
	public static void qrcode(String url, String destPath, int width) {
		File destFile = new File(destPath);
		qrcode(url, destFile, width);
	}

	/**
	 * QRCode 이미지를 생성한다.
	 * @param url QRCode 스캔 시 이동할 곳의 URL
	 * @param destFile QRCode 이미지 파일 객체
	 * @param width QRCode 이미지 길이
	 */
	public static void qrcode(String url, File destFile, int width) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(destFile);
			qrcode(url, fos, width);
			fos.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * QRCode 이미지를 생성한다.
	 * @param url QRCode 스캔 시 이동할 곳의 URL
	 * @param response qrcode 이미지를 전송할 응답객체
	 * @param width QRCode 이미지 길이
	 */
	public static void qrcode(String url, HttpServletResponse response, int width) {
		try {
			response.reset();
			response.setContentType("image/png");
			qrcode(url, response.getOutputStream(), width);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * QRCode 이미지를 생성한다.
	 * @param url QRCode 스캔 시 이동할 곳의 URL
	 * @param os 출력 스트림
	 * @param width QRCode 이미지 길이
	 */
	public static void qrcode(String url, OutputStream os, int width) {
		QRCodeWriter qrWriter = new QRCodeWriter();
		try {
			String encodedUrl = new String(url.getBytes("UTF-8"), "ISO-8859-1");
			BitMatrix bitMatrix = qrWriter.encode(encodedUrl, BarcodeFormat.QR_CODE, width, width);
			MatrixToImageWriter.writeToStream(bitMatrix, "png", os);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 바코드 이미지를 생성한다.
	 * @param num 생성할 바코드 번호
	 * @param destPath 바코드 이미지 파일명
	 * @param width 바코드 이미지 가로 길이
	 * @param height 바코드 이미지 세로길이
	 */
	public static void barcode(String num, String destPath, int width, int height) {
		barcode(num, new File(destPath), width, height);
	}

	/**
	 * 바코드 이미지를 생성한다.
	 * @param num 생성할 바코드 번호
	 * @param destFile 바코드 이미지 파일 객체
	 * @param width 바코드 이미지 가로길이
	 * @param height 바코드 이미지 세로길이
	 */
	public static void barcode(String num, File destFile, int width, int height) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(destFile);
			barcode(num, fos, width, height);
			fos.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * 바코드 이미지를 생성한다.
	 * @param num 생성할 바코드 번호
	 * @param response 바코드 이미지를 전송할 응답객체
	 * @param width 바코드 이미지 가로길이
	 * @param height 바코드 이미지 세로길이
	 */
	public static void barcode(String num, HttpServletResponse response, int width, int height) {
		try {
			response.reset();
			response.setContentType("image/png");
			barcode(num, response.getOutputStream(), width, height);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 바코드 이미지를 생성한다.
	 * @param num 생성할 바코드 번호
	 * @param os 출력 스트림
	 * @param width 바코드 이미지 가로길이
	 * @param height 바코드 이미지 세로길이
	 */
	public static void barcode(String num, OutputStream os, int width, int height) {
		MultiFormatWriter barcodeWriter = new MultiFormatWriter();
		try {
			BitMatrix bitMatrix = barcodeWriter.encode(num, BarcodeFormat.CODE_128, width, height);
			MatrixToImageWriter.writeToStream(bitMatrix, "png", os);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////Private 메소드

	/**
	 * 이미지를 회전한다.
	 * 소스 이미지 파일의 크기는 유지한채 이미지를 회전한다.
	 * @param srcFile 소스 이미지 파일
	 * @param destFile 대상 이미지 파일
	 * @param rotation 각도(90, 180, 270)
	 */
	private static void rotate(File srcFile, File destFile, int rotation) {
		BufferedImage rotatedImg = null;
		try {
			BufferedImage image = ImageIO.read(srcFile);
			double x = 0, y = 0;
			if (rotation % 360 == 90) {
				x = image.getHeight();
				rotatedImg = new BufferedImage(image.getHeight(), image.getWidth(), image.getType());
			} else if (rotation % 360 == 270) {
				y = image.getWidth();
				rotatedImg = new BufferedImage(image.getHeight(), image.getWidth(), image.getType());
			} else if (rotation % 360 == 180) {
				x = image.getWidth();
				y = image.getHeight();
				rotatedImg = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
			} else {
				rotatedImg = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
			}
			Graphics2D g2d = rotatedImg.createGraphics();
			g2d.translate(x, y);
			g2d.rotate(Math.toRadians(rotation));
			g2d.drawImage(image, 0, 0, null);
			g2d.dispose();
			ImageIO.write(rotatedImg, "jpg", destFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (rotatedImg != null) {
				rotatedImg.flush();
			}
		}
	}

	/**
	 * 원본 이미지 사이즈와 리사이즈할 사이즈로 이미지 스케일 비율을 구한다.
	 * 크기가 큰 폭을 기준으로 동일 비율로 한다.
	 * @param resizeWidth 리사이즈할 가로 사이즈
	 * @param resizeHeight 리사이즈할 세로 사이즈
	 * @param imageWidth 원본 이미지의 가로 사이즈
	 * @param imageHeight 원본 이미지의 세로 사이즈
	 * @return 스케일 비율
	 */
	private static double getScale(int resizeWidth, int resizeHeight, int imageWidth, int imageHeight) {
		double widthScale = (double) resizeWidth / imageWidth;
		double heightScale = (double) resizeHeight / (double) imageHeight;
		if (widthScale > heightScale) {
			return heightScale;
		} else {
			return widthScale;
		}
	}

	/**
	 * 원본 이미지 사이즈와 리사이즈할 사이즈로 이미지 스케일 비율을 구한다.
	 * 크기가 큰 쪽을 기준으로 동일 비율로 한다.
	 * @param size 리사이즈할  사이즈
	 * @param imageSize 원본 이미지의  사이즈
	 * @return 스케일 비율
	 */
	private static double getScale(int size, int imageSize) {
		double scale = (double) size / imageSize;
		return scale;
	}
}