package audit;

import configs.Codes;
import configs.SystemDate;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.io.FileWriter;
import java.io.IOException;

public class AuditService {
    private static final Map<UserType, String> fileNames;
    private static final Map<UserType, FileWriter> files;

    static {
        fileNames = new HashMap<>();
        fileNames.put(UserType.CUSTOMER, "./audit/logging_for_customers.csv");
        fileNames.put(UserType.BANK_MANAGER, "./audit/logging_for_bank_management.csv");

        files = new HashMap<>();
        AuditService.openFiles();
    }

    private static void openFiles() {
        AuditService.fileNames.entrySet().stream().forEach((entry) -> {
            try {
                FileWriter file = new FileWriter(entry.getValue(), true);
                AuditService.files.put(entry.getKey(), file);
                if (Files.size(Paths.get(entry.getValue())) == 0) {
                    file.write(String.join(",", "timestamp", "action").toUpperCase() + "\n");
                    file.flush();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                System.exit(Codes.EXIT_ON_ERROR);
            }
        });
    }

    public static void closeFiles() {
        AuditService.files.values().forEach((file) -> {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static String getTimeStamp() {
        // voi folosi system date
        // aplicatia este dezvoltata astfel incat sa pot schimba data curenta a aplicatiei
        // folosesc real data doar pentru ora si minut
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        final LocalDate systemDate = SystemDate.getDate();
        final LocalDateTime realDate = LocalDateTime.now();

        return realDate.withYear(systemDate.getYear()).withMonth(systemDate.getMonthValue()).withDayOfMonth(systemDate.getDayOfMonth()).format(dateFormatter);
    }

    private static void writeToFile(FileWriter file, String message) {
        String timestamp = AuditService.getTimeStamp();
        String line = String.join(",", timestamp, message) + "\n";
        try {
            file.write(line);
            file.flush();
        } catch (IOException exception) {
            exception.printStackTrace();
            System.exit(Codes.EXIT_ON_ERROR);
        }

    }

    public static void addLoggingData(UserType userType, String message) {
        FileWriter file = AuditService.files
                .entrySet()
                .stream()
                .filter(el -> el.getKey() == userType)
                .map(Map.Entry::getValue)
                .findFirst()
                .get();
        AuditService.writeToFile(file, message);
    }


}