package it.pagopa.firmaconio.firma_qtsp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "azure-blob")
public class AzureBlobProperties {
    private String blobConnectionString;

    public String getConnectionString() {
        return blobConnectionString;
    }

    public void setConnectionString(String blobConnectionString) {
        this.blobConnectionString = blobConnectionString;
    }
}
