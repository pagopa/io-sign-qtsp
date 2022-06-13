package it.pagopa.firmaconio.firma_qtsp.service;

import eu.europa.esig.dss.cades.CMSUtils;
import eu.europa.esig.dss.cades.signature.CustomContentSigner;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.EncryptionAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.spi.DSSASN1Utils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import it.pagopa.firmaconio.firma_qtsp.config.AzureBlobProperties;
import it.pagopa.firmaconio.firma_qtsp.utility.PadesCMSSignedDataBuilder;

import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.SignerInfoGeneratorBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.util.Objects;

/* This is a mock service for QTSP!
 * This service will be replaced by the http call to the QTSP
 */
public class QTSPService {

        AzureBlobProperties azureBloblProperties;

        @Autowired
        public QTSPService(AzureBlobProperties azureBloblProperties) {
                this.azureBloblProperties = azureBloblProperties;
        }

        private static PadesCMSSignedDataBuilder padesCMSSignedDataBuilder;
        CloudStorageAccount storageAccountDest;
        CloudBlobClient blobClientDest = null;
        CloudBlobContainer containerDest = null;

        private static String certFileName = "my_cert.p12";
        private static String certAliasKey = "my sign cert";
        private static String containerName = "signcert";

        public File getCert() throws URISyntaxException, InvalidKeyException, StorageException, IOException {

                String storageConnectionStringDest = azureBloblProperties.getConnectionString();
                storageAccountDest = CloudStorageAccount.parse(storageConnectionStringDest);
                blobClientDest = storageAccountDest.createCloudBlobClient();
                containerDest = blobClientDest.getContainerReference(containerName);
                CloudBlob blobDest = containerDest.getBlockBlobReference(certFileName);
                File tmpFile = Files.createTempFile(certFileName, null).toFile();

                blobDest.downloadToFile(tmpFile.getPath());
                return tmpFile;
        }

        public byte[] signHash(byte[] hash)
                        throws IOException, URISyntaxException, InvalidKeyException, StorageException {

                File certFile = this.getCert();
                Pkcs12SignatureToken signingToken = new Pkcs12SignatureToken(certFile.getPath(),
                                new KeyStore.PasswordProtection("".toCharArray()));

                DSSPrivateKeyEntry privateKey = signingToken.getKey(certAliasKey);
                CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
                padesCMSSignedDataBuilder = new PadesCMSSignedDataBuilder(commonCertificateVerifier);

                PAdESSignatureParameters parameters = new PAdESSignatureParameters();
                parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
                parameters.setEncryptionAlgorithm(EncryptionAlgorithm.RSA);
                parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
                parameters.setSigningCertificate(privateKey.getCertificate());

                ToBeSigned dataToSign = getDataToSign(hash, parameters);
                SignatureValue signatureValue = signingToken.sign(dataToSign, DigestAlgorithm.SHA256, privateKey);
                return generateCMSSignedData(hash, parameters, signatureValue);
        }

        public static ToBeSigned getDataToSign(byte[] messageDigest, final PAdESSignatureParameters parameters)
                        throws DSSException {
                final SignatureAlgorithm signatureAlgorithm = parameters.getSignatureAlgorithm();
                final CustomContentSigner customContentSigner = new CustomContentSigner(signatureAlgorithm.getJCEId());

                SignerInfoGeneratorBuilder signerInfoGeneratorBuilder = padesCMSSignedDataBuilder
                                .getSignerInfoGeneratorBuilder(parameters, messageDigest);

                final CMSSignedDataGenerator generator = padesCMSSignedDataBuilder.createCMSSignedDataGenerator(
                                parameters,
                                customContentSigner,
                                signerInfoGeneratorBuilder, null);

                final CMSProcessableByteArray content = new CMSProcessableByteArray(messageDigest);

                CMSUtils.generateDetachedCMSSignedData(generator, content);

                final byte[] dataToSign = customContentSigner.getOutputStream().toByteArray();
                return new ToBeSigned(dataToSign);
        }

        protected static byte[] generateCMSSignedData(byte[] messageDigest, final PAdESSignatureParameters parameters,
                        final SignatureValue signatureValue) {
                final SignatureAlgorithm signatureAlgorithm = parameters.getSignatureAlgorithm();
                final SignatureLevel signatureLevel = parameters.getSignatureLevel();
                Objects.requireNonNull(signatureAlgorithm, "SignatureAlgorithm cannot be null!");
                Objects.requireNonNull(signatureLevel, "SignatureLevel must be defined!");

                final CustomContentSigner customContentSigner = new CustomContentSigner(signatureAlgorithm.getJCEId(),
                                signatureValue.getValue());

                final SignerInfoGeneratorBuilder signerInfoGeneratorBuilder = padesCMSSignedDataBuilder
                                .getSignerInfoGeneratorBuilder(parameters, messageDigest);

                final CMSSignedDataGenerator generator = padesCMSSignedDataBuilder.createCMSSignedDataGenerator(
                                parameters,
                                customContentSigner,
                                signerInfoGeneratorBuilder, null);

                final CMSProcessableByteArray content = new CMSProcessableByteArray(messageDigest);
                CMSSignedData data = CMSUtils.generateDetachedCMSSignedData(generator, content);

                return DSSASN1Utils.getDEREncoded(data);
        }

}
