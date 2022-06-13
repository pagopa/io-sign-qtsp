package it.pagopa.firmaconio.firma_qtsp.utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;



public class FileUtility {

    public static final String createDownloadLink(File file) {
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/download/")
                .path(file.getName())
                .toUriString();
        return fileDownloadUri;
    }

    public static final String calculateSize(File file) {
        try {
            long bytes;
            bytes = Files.size(file.toPath());
            return String.format("%,d kilobytes", bytes / 1024);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return "INVALID_SIZE";
        }
       
    }
}
