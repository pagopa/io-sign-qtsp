package it.pagopa.firmaconio.firma_qtsp.model;

import java.io.File;
import java.util.UUID;

import com.fasterxml.jackson.annotation.*;

import it.pagopa.firmaconio.firma_qtsp.utility.FileUtility;

@JsonIgnoreProperties(value = "file")
public class FileTBS {

    private UUID id;
    private File file;
    private String hash;
    private String signedHash;
    private String size;
    private Boolean signed = false;

    public FileTBS(UUID id, File tmpFile) {
        this.id = id;
        this.file = tmpFile;
        this.size = FileUtility.calculateSize(this.file);
    }

    public File getFile() {
        return this.file;
    }

    public String getDownloadLink() {
        return FileUtility.createDownloadLink(this.file);
    }

    public String getHash() {
        return hash;
    }

    public String getSignedHash() {
        return signedHash;
    }

    public String getSize() {
        return size;
    }

    public Boolean isSigned() {
        return signed;
    }

    public UUID getId() {
        return id;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setSignedHash(String signedHash) {
        this.signedHash = signedHash;
    }

    public void setSigned(Boolean signed) {
        this.signed = signed;
    }
}
