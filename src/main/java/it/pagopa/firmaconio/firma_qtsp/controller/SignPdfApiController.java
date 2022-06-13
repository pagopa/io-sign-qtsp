package it.pagopa.firmaconio.firma_qtsp.controller;

import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import it.pagopa.firmaconio.firma_qtsp.api.SignPdfApi;
import it.pagopa.firmaconio.firma_qtsp.exception.UploadedFileException;
import it.pagopa.firmaconio.firma_qtsp.model.FileTBS;
import it.pagopa.firmaconio.firma_qtsp.model.SignResponse;
import it.pagopa.firmaconio.firma_qtsp.service.FileStorageService;
import it.pagopa.firmaconio.firma_qtsp.service.PadesService;
import it.pagopa.firmaconio.firma_qtsp.utility.FileUtility;

@RestController
public class SignPdfApiController implements SignPdfApi {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PadesService padesService;

    @Override
    public ResponseEntity<SignResponse> signPdf(MultipartFile fileToBeSigned) {

        if (fileToBeSigned.isEmpty())
            throw new UploadedFileException("The uploaded file is empty.");

        if (!fileToBeSigned.getContentType().equals("application/pdf"))
            throw new UploadedFileException("The uploaded file does not appear to be a valid pdf.");

        File tmpFile = fileStorageService.storeFile(fileToBeSigned);
        padesService.setFile(tmpFile);
        FileTBS fileTbs = padesService.signFile();
        SignResponse response = new SignResponse();
        response.setHash(fileTbs.getHash());
        response.setSignedHash(fileTbs.getSignedHash());
        response.setSigned(fileTbs.isSigned());
        response.setSignedFileUrl(FileUtility.createDownloadLink(fileTbs.getFile()));
        return ResponseEntity.ok(response);

    }

}