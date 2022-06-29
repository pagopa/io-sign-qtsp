package it.pagopa.firmaconio.firma_qtsp.service;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pdf.IPdfObjFactory;
import eu.europa.esig.dss.pdf.PDFSignatureService;
import eu.europa.esig.dss.pdf.ServiceLoaderPdfObjFactory;
import it.pagopa.firmaconio.firma_qtsp.exception.QtspException;
import it.pagopa.firmaconio.firma_qtsp.model.FileTBS;
import it.pagopa.firmaconio.firma_qtsp.utility.UUIDUtility;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Base64;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PadesService {

    private FileTBS fileTBS;

    private QTSPService QTSPService;

    @Autowired
    public PadesService() throws InvalidKeyException, URISyntaxException, IOException {

        this.QTSPService = new QTSPService();
    }

    public FileTBS getFileTbs() {
        return this.fileTBS;
    }

    public void setFile(@NotNull File file) {
        UUID uuid = UUIDUtility.fileToUUID(file);
        this.fileTBS = new FileTBS(uuid, file);
    }

    /*
     * This method calculate the hash of the document, call the QTSPService to sign
     * hash and create a final PADES PDF
     */
    public FileTBS signFile() {

        try {
            FileDocument documentToSign = new FileDocument(this.fileTBS.getFile());
            PAdESSignatureParameters parameters = new PAdESSignatureParameters();
            parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
            parameters.setGenerateTBSWithoutCertificate(true);

            IPdfObjFactory pdfObjFactory = new ServiceLoaderPdfObjFactory();
            PDFSignatureService pdfSignatureService = pdfObjFactory.newPAdESSignatureService();

            byte[] hash = pdfSignatureService.digest(documentToSign, parameters);
            String calculatedHash = Base64.getEncoder().encodeToString(hash);
            this.fileTBS.setHash(calculatedHash);

            byte[] signedHash = QTSPService.signHash(hash);
            String calculatedSignedHash = Base64.getEncoder().encodeToString(signedHash);
            this.fileTBS.setSignedHash(calculatedSignedHash);

            DSSDocument signedDocument = pdfSignatureService.sign(documentToSign, signedHash, parameters);
            signedDocument.save(this.fileTBS.getFile().getPath());
            this.fileTBS.setSigned(true);

            return this.fileTBS;

        } catch (IOException e) {
            throw new QtspException("QTSP error: I can't read the files");
        } catch (URISyntaxException e) {
            throw new QtspException("QTSP error: URL not found");
        } catch (InvalidKeyException e) {
            throw new QtspException("QTSP error: Invalid Key");
        }

    }

}
