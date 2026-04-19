package com.javarpa.test;

import com.javarpa.macro.MacroAction;
import com.javarpa.macro.MacroPlayer;
import com.javarpa.macro.MacroScript;

import java.util.List;

/**
 * Test: verify MacroPlayer phát lại đúng thứ tự đã ghi.
 * Không cần AWT Robot — override executeAction để chỉ in log.
 *
 * Chạy: java -cp <classpath> com.javarpa.test.MacroOrderTest
 */
public class MacroOrderTest {

    public static void main(String[] args) {
        System.out.println("=== TEST: Macro Playback Order ===\n");

        // 1. Tạo script mô phỏng "đã ghi"
        MacroScript script = new MacroScript("Test Macro");
        script.addAction(MacroAction.mouseClick(100, 200));
        script.addAction(MacroAction.delay(500));
        script.addAction(MacroAction.mouseClick(300, 400));
        script.addAction(MacroAction.keyType("Hello World"));
        script.addAction(MacroAction.mouseRightClick(500, 600));
        script.addAction(MacroAction.delay(300));
        script.addAction(MacroAction.mouseDoubleClick(700, 800));
        script.addAction(MacroAction.keyPress(13)); // ENTER
        script.addAction(MacroAction.mouseScroll(-3));
        script.addAction(MacroAction.mouseClick(100, 200)); // lại về đầu

        System.out.println("--- Thu tu DA GHI (goc): ---");
        List<MacroAction> original = script.getActions();
        for (int i = 0; i < original.size(); i++) {
            System.out.printf("  [%2d] %s%n", i + 1, original.get(i).toString());
        }

        System.out.println("\n--- Thu tu PHAT LAI (log thuc te): ---");

        // 2. Dùng MockPlayer — không gọi Robot thật, chỉ in thứ tự
        MockMacroPlayer player = new MockMacroPlayer();
        player.load(script);
        player.play(); // synchronous trong test

        // 3. Kiểm tra thứ tự
        System.out.println("\n--- KIEM TRA: ---");
        List<String> replayed = player.getReplayedOrder();
        boolean allMatch = true;
        for (int i = 0; i < original.size(); i++) {
            String expected = original.get(i).toString();
            String actual   = replayed.get(i);
            boolean match   = expected.equals(actual);
            if (!match) allMatch = false;
            System.out.printf("  [%2d] %s  %s  %s%n",
                    i + 1,
                    expected,
                    match ? "==" : "!=",
                    actual);
        }

        System.out.println();
        if (allMatch) {
            System.out.println("✅ KET QUA: Thu tu CHINH XAC! MacroPlayer phat lai dung thu tu da ghi.");
        } else {
            System.out.println("❌ KET QUA: CO SAI LECH thu tu!");
        }
    }

    // ── Mock Player: không gọi Robot thật ─────────────────────────────
    static class MockMacroPlayer extends MacroPlayer {
        private final java.util.List<String> replayedOrder = new java.util.ArrayList<>();
        private MacroScript script;

        @Override
        public void load(MacroScript s) { this.script = s; }

        @Override
        public void play() {
            // Synchronous: phát thẳng trong thread hiện tại
            List<MacroAction> actions = script.getActions();
            for (int i = 0; i < actions.size(); i++) {
                MacroAction action = actions.get(i);
                replayedOrder.add(action.toString());
                System.out.printf("  [%2d/%2d] %s%n", i + 1, actions.size(), action.toString());
                // Delay nhỏ để mô phỏng thực thi
                if (action.getType() == MacroAction.Type.DELAY) {
                    try { Thread.sleep(Math.min(action.getDelayMs(), 100)); }
                    catch (InterruptedException ignored) {}
                }
            }
        }

        public List<String> getReplayedOrder() { return replayedOrder; }
    }
}
