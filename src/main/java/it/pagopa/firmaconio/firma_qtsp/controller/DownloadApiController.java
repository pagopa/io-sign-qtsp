package it.pagopa.firmaconio.firma_qtsp.controller;

import java.io.IOException;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.pagopa.firmaconio.firma_qtsp.api.DownloadApi;
import it.pagopa.firmaconio.firma_qtsp.service.FileStorageService;

@RestController
public class DownloadApiController implements DownloadApi {

   @Autowired
   private FileStorageService fileStorageService;

   @Override
   public ResponseEntity<Resource> downloadPdf(String fileName) {
      // Load file as Resource
      Resource resource = fileStorageService.loadFileAsResource(fileName);

      // Try to determine file's content type
      String contentType;

      try {
         contentType = Files.probeContentType(resource.getFile().toPath());
      } catch (IOException ex) {
         contentType = "application/octet-stream";
      }

      return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
   }
}
