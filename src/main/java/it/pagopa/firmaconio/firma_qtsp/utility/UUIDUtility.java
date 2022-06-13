package it.pagopa.firmaconio.firma_qtsp.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

public class UUIDUtility {
    public static final UUID fileToUUID(File file) {
        try {
            FileInputStream fl = new FileInputStream(file);
            byte[] arr = new byte[(int) file.length()];
            fl.read(arr);
            fl.close();
            return UUID.nameUUIDFromBytes(arr);
        } catch (IOException e) {
            return UUID.randomUUID();
        }
    }
}
