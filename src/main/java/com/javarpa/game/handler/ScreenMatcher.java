package com.javarpa.game.handler;

import com.javarpa.core.ScreenCapture;
import nu.pattern.OpenCV;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.*;

/**
 * Template Matching giống PyAutoGUI cho Java.
 *
 * <p>Sử dụng OpenCV {@code Imgproc.matchTemplate()} để tìm ảnh mẫu
 * trên màn hình — giống hệt {@code pyautogui.locateOnScreen()}.</p>
 *
 * <h3>Workflow:</h3>
 * <ol>
 *   <li>User mở game → đến màn hình login</li>
 *   <li>Click "📸 Chụp mẫu Login" → lưu ảnh vùng nút Đăng Nhập</li>
 *   <li>Bot chụp screen → tìm ảnh mẫu trong screenshot</li>
 *   <li>Khi tìm thấy (confidence ≥ 0.75) → login screen đã hiển thị</li>
 * </ol>
 */
public class ScreenMatcher {

    /** Ngưỡng matching: ≥ 75% → xác nhận screen xuất hiện. */
    private static final double MATCH_THRESHOLD = 0.75;

    /** Kích thước vùng capture xung quanh điểm trung tâm (cho chụp mẫu). */
    private static final int REGION_W = 160;
    private static final int REGION_H = 60;

    /** Thư mục lưu ảnh mẫu. */
    private static final Path REFS_DIR = Paths.get(
        System.getProperty("user.home"), ".javarpa", "refs"
    );

    /** Flag: OpenCV đã load chưa. */
    private static volatile boolean opencvLoaded = false;

    // ================================================================
    //  LOAD OPENCV
    // ================================================================

    /**
     * Load OpenCV native library (tự động cho Windows/Linux/Mac).
     * Chỉ cần gọi 1 lần — safe to call multiple times.
     */
    private static synchronized void ensureOpenCVLoaded() {
        if (!opencvLoaded) {
            try {
                OpenCV.loadLocally();
                opencvLoaded = true;
                System.out.println("[ScreenMatcher] OpenCV loaded OK: " + Core.VERSION);
            } catch (Exception e) {
                System.err.println("[ScreenMatcher] OpenCV load failed: " + e.getMessage());
                throw new RuntimeException("Cannot load OpenCV", e);
            }
        }
    }

    // ================================================================
    //  LƯU ẢNH MẪU
    // ================================================================

    /**
     * Chụp ảnh mẫu vùng xung quanh (centerX, centerY) và lưu vào file.
     *
     * @param centerX  tọa độ X trung tâm (VD: loginBtnX)
     * @param centerY  tọa độ Y trung tâm (VD: loginBtnY)
     * @param refName  tên reference (VD: "login_screen")
     * @return true nếu lưu thành công
     */
    public static boolean captureReference(int centerX, int centerY, String refName) {
        try {
            Rectangle region = buildRegion(centerX, centerY);
            BufferedImage img = ScreenCapture.captureRegion(region);
            Path path = getRefPath(refName);
            Files.createDirectories(path.getParent());
            ImageIO.write(img, "png", path.toFile());
            return true;
        } catch (Exception e) {
            System.err.println("[ScreenMatcher] Capture failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra đã có ảnh mẫu chưa.
     */
    public static boolean hasReference(String refName) {
        return Files.exists(getRefPath(refName));
    }

    // ================================================================
    //  LOCATE ON SCREEN (giống PyAutoGUI)
    // ================================================================

    /**
     * Tìm ảnh mẫu trên màn hình — giống {@code pyautogui.locateOnScreen()}.
     *
     * @param refName tên ảnh mẫu đã lưu
     * @return LocateResult chứa vị trí và confidence, null nếu không tìm thấy
     */
    public static LocateResult locateOnScreen(String refName) {
        return locateOnScreen(refName, MATCH_THRESHOLD);
    }

    /**
     * Tìm ảnh mẫu trên màn hình với ngưỡng tùy chỉnh.
     *
     * @param refName    tên ảnh mẫu
     * @param confidence ngưỡng matching (0.0 → 1.0)
     * @return LocateResult hoặc null nếu không tìm thấy / lỗi
     */
    public static LocateResult locateOnScreen(String refName, double confidence) {
        try {
            ensureOpenCVLoaded();

            Path refPath = getRefPath(refName);
            if (!Files.exists(refPath)) return null;

            // Đọc ảnh mẫu bằng OpenCV
            Mat template = Imgcodecs.imread(refPath.toString());
            if (template.empty()) return null;

            // Chụp toàn màn hình → convert sang OpenCV Mat
            BufferedImage screenshot = ScreenCapture.captureFullScreen();
            Mat screen = bufferedImageToMat(screenshot);

            // Template matching
            Mat result = new Mat();
            Imgproc.matchTemplate(screen, template, result, Imgproc.TM_CCOEFF_NORMED);

            // Tìm điểm match cao nhất
            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

            // Cleanup
            template.release();
            screen.release();
            result.release();

            if (mmr.maxVal >= confidence) {
                int x = (int) mmr.maxLoc.x;
                int y = (int) mmr.maxLoc.y;
                return new LocateResult(x, y, REGION_W, REGION_H, mmr.maxVal);
            }
            return null;

        } catch (Exception e) {
            System.err.println("[ScreenMatcher] locateOnScreen failed: " + e.getMessage());
            return null;
        }
    }

    // ================================================================
    //  SO SÁNH VÙNG CỐ ĐỊNH (nhanh hơn locateOnScreen)
    // ================================================================

    /**
     * So sánh vùng cố định trên screen với ảnh mẫu.
     * Nhanh hơn locateOnScreen() vì chỉ check 1 vùng nhỏ.
     *
     * @param centerX  tọa độ X trung tâm
     * @param centerY  tọa độ Y trung tâm
     * @param refName  tên ảnh mẫu
     * @return confidence [0.0 → 1.0], hoặc -1 nếu lỗi
     */
    public static double matchScreen(int centerX, int centerY, String refName) {
        try {
            ensureOpenCVLoaded();

            Path refPath = getRefPath(refName);
            if (!Files.exists(refPath)) return -1;

            Mat template = Imgcodecs.imread(refPath.toString());
            if (template.empty()) return -1;

            // Chụp vùng cố định
            Rectangle region = buildRegion(centerX, centerY);
            BufferedImage regionImg = ScreenCapture.captureRegion(region);
            Mat regionMat = bufferedImageToMat(regionImg);

            // So sánh — vì kích thước bằng nhau, matchTemplate sẽ ra 1 giá trị
            Mat result = new Mat();
            Imgproc.matchTemplate(regionMat, template, result, Imgproc.TM_CCOEFF_NORMED);
            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

            template.release();
            regionMat.release();
            result.release();

            return mmr.maxVal;
        } catch (Exception e) {
            System.err.println("[ScreenMatcher] matchScreen failed: " + e.getMessage());
            return -1;
        }
    }

    // ================================================================
    //  CHỜ CHO ĐẾN KHI TÌM THẤY
    // ================================================================

    /**
     * Chờ cho đến khi ảnh mẫu xuất hiện trên screen (polling).
     * Giống {@code pyautogui.locateOnScreen()} trong vòng loop.
     *
     * @param centerX   tọa độ X (dùng matchScreen nhanh), 0 = dùng locateOnScreen toàn screen
     * @param centerY   tọa độ Y
     * @param refName   tên ảnh mẫu
     * @param timeoutMs thời gian chờ tối đa
     * @param pollMs    khoảng cách giữa các lần poll
     * @return MatchResult
     */
    public static MatchResult waitForMatch(int centerX, int centerY,
                                            String refName, long timeoutMs, long pollMs) {
        try {
            ensureOpenCVLoaded();

            Path refPath = getRefPath(refName);
            if (!Files.exists(refPath)) {
                return new MatchResult(false, 0, 0, "Chưa có ảnh mẫu: " + refName);
            }

            long elapsed = 0;
            double lastSimilarity = 0;

            while (elapsed < timeoutMs) {
                if (centerX > 0 && centerY > 0) {
                    // Chế độ nhanh: chỉ check vùng cố định
                    lastSimilarity = matchScreen(centerX, centerY, refName);
                } else {
                    // Chế độ full: tìm trên toàn màn hình
                    LocateResult loc = locateOnScreen(refName);
                    lastSimilarity = (loc != null) ? loc.confidence : 0;
                }

                if (lastSimilarity >= MATCH_THRESHOLD) {
                    return new MatchResult(true, lastSimilarity, elapsed, null);
                }

                Thread.sleep(pollMs);
                elapsed += pollMs;
            }

            return new MatchResult(false, lastSimilarity, elapsed,
                "Timeout " + (timeoutMs / 1000) + "s");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new MatchResult(false, 0, 0, "Interrupted");
        } catch (Exception e) {
            return new MatchResult(false, 0, 0, e.getMessage());
        }
    }

    // ================================================================
    //  UTILITIES
    // ================================================================

    /**
     * Convert BufferedImage (Java AWT) → Mat (OpenCV).
     */
    private static Mat bufferedImageToMat(BufferedImage img) {
        // Đảm bảo ảnh là TYPE_3BYTE_BGR (format OpenCV cần)
        BufferedImage converted = new BufferedImage(
            img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR
        );
        converted.getGraphics().drawImage(img, 0, 0, null);

        byte[] pixels = ((DataBufferByte) converted.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(converted.getHeight(), converted.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        return mat;
    }

    private static Rectangle buildRegion(int centerX, int centerY) {
        int x = Math.max(0, centerX - REGION_W / 2);
        int y = Math.max(0, centerY - REGION_H / 2);
        return new Rectangle(x, y, REGION_W, REGION_H);
    }

    private static Path getRefPath(String refName) {
        String safe = refName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return REFS_DIR.resolve(safe + ".png");
    }

    /**
     * Trả path ảnh mẫu (dùng hiện thông tin trên UI).
     */
    public static String getRefPathString(String refName) {
        return getRefPath(refName).toString();
    }

    // ================================================================
    //  RESULT CLASSES
    // ================================================================

    /** Kết quả tìm ảnh trên screen (giống pyautogui.locateOnScreen). */
    public static class LocateResult {
        public final int x, y, width, height;
        public final double confidence;

        public LocateResult(int x, int y, int width, int height, double confidence) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.confidence = confidence;
        }

        /** Tọa độ trung tâm (giống pyautogui.locateCenterOnScreen). */
        public int centerX() { return x + width / 2; }
        public int centerY() { return y + height / 2; }

        @Override
        public String toString() {
            return String.format("(%d, %d) %dx%d conf=%.1f%%",
                x, y, width, height, confidence * 100);
        }
    }

    /** Kết quả chờ matching. */
    public static class MatchResult {
        public final boolean matched;
        public final double  similarity;
        public final long    elapsedMs;
        public final String  error;

        public MatchResult(boolean matched, double similarity, long elapsedMs, String error) {
            this.matched    = matched;
            this.similarity = similarity;
            this.elapsedMs  = elapsedMs;
            this.error      = error;
        }
    }
}
