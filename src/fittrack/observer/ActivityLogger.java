package fittrack.observer;

import fittrack.singleton.AppConfig;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Observer Pattern – logs every fitness event to file. */
public class ActivityLogger implements FitnessObserver {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onDataChanged(String event, Object data) {
        String msg = String.format("[%s] %s | %s",
                LocalDateTime.now().format(FMT), event, data != null ? data.toString() : "");
        System.out.println("FITLOG: " + msg);
        try (PrintWriter pw = new PrintWriter(new FileWriter(
                AppConfig.getInstance().getLogFilePath(), true))) {
            pw.println(msg);
        } catch (IOException e) { /* ignore */ }
    }
}
