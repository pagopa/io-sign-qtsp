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
import it.pagopa.firmaconio.firma_qtsp.utility.PadesCMSSignedDataBuilder;


import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.SignerInfoGeneratorBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.util.Objects;

/* This is a mock service for QTSP!
 * This service will be replaced by the http call to the QTSP
 */
public class QTSPService {

        @Autowired
        public QTSPService() {
        }

        private static PadesCMSSignedDataBuilder padesCMSSignedDataBuilder;

        private static String certFileName = "fake_cert.p12";
        private static String certAliasKey = "my sign cert";

        public File getCert() throws URISyntaxException, InvalidKeyException, IOException {
                File myCert = new File(certFileName);
                return myCert;
        }

        public byte[] signHash(byte[] hash)
                        throws IOException, URISyntaxException, InvalidKeyException {

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
