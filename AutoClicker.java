import java.awt.*;
import java.awt.event.InputEvent;
import java.util.Scanner;
import java.util.concurrent.*;

public class AutoClicker {

    private static final int CLICK_INTERVAL_MS = 100;
    private static final int ZONE_SIZE = 100;

    public static void main(String[] args) throws Exception {
        System.out.println("🖱️ Наведи мышь на центр желаемого квадрата...");
        System.out.println("⏳ Через 5 секунд запомним координаты...");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                Point center = MouseInfo.getPointerInfo().getLocation();
                System.out.printf("📍 Центр зоны: (%d, %d)%n", center.x, center.y);
                startClickingWhenInZone(center);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5, TimeUnit.SECONDS);

        future.get();
    }

    private static void startClickingWhenInZone(Point center) throws AWTException {
        Robot robot = new Robot();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        Rectangle zone = new Rectangle(
                center.x - ZONE_SIZE / 2,
                center.y - ZONE_SIZE / 2,
                ZONE_SIZE,
                ZONE_SIZE
        );

        System.out.printf("✅ Зона отслеживания: %s%n", zone);
        System.out.println("🖱️ Клик будет выполняться, только если курсор внутри этой зоны.");
        System.out.println("Нажми Enter, чтобы остановить.");

        Runnable clickTask = () -> {
            Point current = MouseInfo.getPointerInfo().getLocation();
            if (zone.contains(current)) {
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                System.out.printf("✔ Клик по (%d, %d)%n", current.x, current.y);
            }
        };

        ScheduledFuture<?> clickHandle = executor.scheduleAtFixedRate(
                clickTask,
                0,
                CLICK_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        clickHandle.cancel(true);
        executor.shutdown();
        System.out.println("🛑 Остановлено пользователем.");
    }
}
