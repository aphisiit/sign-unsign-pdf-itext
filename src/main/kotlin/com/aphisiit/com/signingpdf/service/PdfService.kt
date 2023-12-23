package com.aphisiit.com.signingpdf.service

import com.itextpdf.forms.PdfAcroForm
import com.itextpdf.kernel.pdf.*
import com.itextpdf.signatures.BouncyCastleDigest
import com.itextpdf.signatures.PdfSigner
import com.itextpdf.signatures.PrivateKeySignature
import jakarta.xml.bind.DatatypeConverter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.security.KeyFactory
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import javax.net.ssl.KeyManagerFactory

@Service
class PdfService {
        
    private val logger = LoggerFactory.getLogger(this.javaClass)
        
    fun signPdf(inputStream: InputStream) : ByteArray {
        Security.addProvider(BouncyCastleProvider())
        
        val certBytes = File("src/main/resources/certificates/certificate.pem").readBytes()
        val keyBytes = File("src/main/resources/certificates/private_key.pem").readBytes()
            
        val certParsed = parseCertificate(certBytes, "-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----")
        val keyParsed = parseCertificate(keyBytes, "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----")
            
        val cert = generateCertificate(certParsed)
        val key = generatePrivateKey(keyParsed)
            
        val ks = KeyStore.getInstance("JKS")
        ks.load(null)
        ks.setCertificateEntry("cert-alias", cert)
        ks.setKeyEntry("key-alias", key, "changeit".toCharArray(), arrayOf(cert))

        val alias: String = ks.aliases().nextElement()
        val privateKey: PrivateKey = key
        val chain: Array<Certificate> = ks.getCertificateChain(alias)

        val pdfReader = PdfReader(inputStream)
        val dest = ByteArrayOutputStream()
        val signer = PdfSigner(pdfReader, dest, StampingProperties())

        val signatureAppearance = signer.signatureAppearance
        signatureAppearance
            .setReason("Test sign and unsign PDF")
            .setLocation("Nonthaburi")
            .setSignatureCreator("Aphisit Namracha")

        signer.fieldName = "Signature"

        val privateKeySignature = PrivateKeySignature(privateKey, "SHA-256", BouncyCastleProvider.PROVIDER_NAME)
        signer.signDetached(BouncyCastleDigest(), privateKeySignature, chain, null, null, null, 0, PdfSigner.CryptoStandard.CMS)
    
        val byteArray: ByteArray = dest.toByteArray()
        dest.close()
    
        return byteArray
    }
    
    fun unsignPdf(inputStream: InputStream) : ByteArray {
        val dest = ByteArrayOutputStream()
        val pdfWriter = PdfWriter(dest)
        val pdfReader = PdfReader(inputStream)
        val pdfDocument = PdfDocument(pdfReader, pdfWriter)

        val acroForm: PdfAcroForm = PdfAcroForm.getAcroForm(pdfDocument, true)
        acroForm.flattenFields()

        pdfDocument.close()
        
        val buffer = ByteArray(1024)
        var bytesRead: Int
        
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                dest.write(buffer, 0, bytesRead)
        }
        
        inputStream.close()
            
        val byteArray: ByteArray = dest.toByteArray()
        dest.close()
            
        return byteArray
    }
        
        private fun parseCertificate(byte: ByteArray, beginDelimiter: String, endDelimiter: String) : ByteArray {
                val data = String(byte)
                var content = data.split(beginDelimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                content = content[1].split(endDelimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                return DatatypeConverter.parseBase64Binary(content[0])
        }
        
        private fun generateCertificate(cert: ByteArray) : X509Certificate {
                val factory = CertificateFactory.getInstance("X.509")
                return factory.generateCertificate(ByteArrayInputStream(cert)) as X509Certificate
        }
        
        private fun generatePrivateKey(key: ByteArray) : PrivateKey {
                val keySpec = PKCS8EncodedKeySpec(key)
                val factory = KeyFactory.getInstance("RSA")
                return factory.generatePrivate(keySpec)
        }
}