package com.tpgrupal.desarrollo_de_software.service;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import com.tpgrupal.desarrollo_de_software.util.P12Helper;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
public class AfipWsaaClient {
    private static final String URL_WSAA_HOMO = "https://wsaahomo.afip.gov.ar/ws/services/LoginCms";

    public static String[] obtenerTokenYSign(String keyPath, String certPath) throws Exception {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        PrivateKey privateKey = cargarClavePrivada(keyPath);
        X509Certificate cert = cargarCertificado(certPath);
        return obtenerTokenYSign(privateKey, cert);
    }

    // Misma firma que usa FacturacionAfipService. El servicio es siempre wsfe y el ambiente homologacion.
    public static String[] obtenerTokenYSign(String keyPath, String certPath, String servicio, boolean isProduccion, String cuit) throws Exception {
        return obtenerTokenYSign(keyPath, certPath);
    }

    // Certificado en un .p12/.pfx protegido con contrasena
    public static String[] obtenerTokenYSignP12(String p12Path, String p12Password) throws Exception {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        Object[] kc = P12Helper.obtenerClaveYCertificado(p12Path, p12Password);
        return obtenerTokenYSign((PrivateKey) kc[0], (X509Certificate) kc[1]);
    }

    private static String[] obtenerTokenYSign(PrivateKey privateKey, X509Certificate cert) throws Exception {
        // AFIP no entrega un ticket nuevo mientras el anterior siga vigente, por eso se guarda en un archivo
        File cacheFile = new File("ticket-homo-wsfe-" + cert.getSerialNumber().toString(16) + ".properties");
        String[] guardado = leerTicketGuardado(cacheFile);
        if (guardado != null) {
            return guardado;
        }

        ZonedDateTime now = ZonedDateTime.now();
        String genTime = now.minusMinutes(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String expTime = now.plusMinutes(10).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        String traXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<loginTicketRequest version=\"1.0\">" +
                "<header>" +
                "<uniqueId>" + (System.currentTimeMillis() / 1000) + "</uniqueId>" +
                "<generationTime>" + genTime + "</generationTime>" +
                "<expirationTime>" + expTime + "</expirationTime>" +
                "</header>" +
                "<service>wsfe</service>" +
                "</loginTicketRequest>";

        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        ContentSigner sha256Signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC").build(privateKey);
        gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                new JcaDigestCalculatorProviderBuilder().setProvider("BC").build())
                .build(sha256Signer, cert));
        gen.addCertificates(new JcaCertStore(Collections.singletonList(cert)));

        CMSSignedData signedData = gen.generate(new org.bouncycastle.cms.CMSProcessableByteArray(
                traXml.getBytes(StandardCharsets.UTF_8)), true);

        String cmsBase64 = Base64.getEncoder().encodeToString(signedData.getEncoded());

        String soapEnvelope = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                "xmlns:nso=\"http://wsaa.afip.gov.ar/ws/services/LoginCms\">" +
                "<soapenv:Header/><soapenv:Body><nso:loginCms><nso:in0>" + cmsBase64 +
                "</nso:in0></nso:loginCms></soapenv:Body></soapenv:Envelope>";

        String xmlResponse = enviarSoap(URL_WSAA_HOMO, soapEnvelope);

        String xmlLimpio = xmlResponse.replace("&lt;", "<").replace("&gt;", ">");
        String token = xmlLimpio.substring(xmlLimpio.indexOf("<token>") + 7, xmlLimpio.indexOf("</token>"));
        String sign = xmlLimpio.substring(xmlLimpio.indexOf("<sign>") + 6, xmlLimpio.indexOf("</sign>"));

        guardarTicket(cacheFile, token, sign, xmlLimpio);

        return new String[] { token, sign };
    }

    private static String[] leerTicketGuardado(File cacheFile) {
        if (!cacheFile.exists()) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(cacheFile)) {
            Properties props = new Properties();
            props.load(fis);
            long exp = Long.parseLong(props.getProperty("expiration", "0"));
            String token = props.getProperty("token");
            String sign = props.getProperty("sign");
            if (token != null && sign != null && System.currentTimeMillis() < exp - (10 * 60 * 1000)) {
                return new String[] { token, sign };
            }
        } catch (Exception e) {
            // si el archivo esta danado se pide un ticket nuevo
        }
        return null;
    }

    private static void guardarTicket(File cacheFile, String token, String sign, String xmlLimpio) {
        long exp = System.currentTimeMillis() + (11 * 60 * 60 * 1000L);
        int i = xmlLimpio.indexOf("<expirationTime>");
        int f = xmlLimpio.indexOf("</expirationTime>");
        if (i >= 0 && f > i) {
            try {
                exp = ZonedDateTime.parse(xmlLimpio.substring(i + 16, f), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        .toInstant().toEpochMilli();
            } catch (Exception e) {
                // se usa el vencimiento por defecto
            }
        }
        Properties props = new Properties();
        props.setProperty("token", token);
        props.setProperty("sign", sign);
        props.setProperty("expiration", String.valueOf(exp));
        try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
            props.store(fos, "Ticket de Acceso AFIP - wsfe");
        } catch (IOException e) {
            System.err.println("No se pudo guardar el ticket de AFIP: " + e.getMessage());
        }
    }

    private static PrivateKey cargarClavePrivada(String path) throws Exception {
        String content = new String(Files.readAllBytes(new File(path).toPath()));
        content = content.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(content);
        return KeyFactory.getInstance("RSA").generatePrivate(
                new java.security.spec.PKCS8EncodedKeySpec(keyBytes));
    }

    private static X509Certificate cargarCertificado(String path) throws Exception {
        try (FileInputStream fis = new FileInputStream(path)) {
            return (X509Certificate) java.security.cert.CertificateFactory
                    .getInstance("X.509").generateCertificate(fis);
        }
    }

    private static String enviarSoap(String urlStr, String soapXml) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        // ¡Esta es la cabecera que exigía AFIP!
        conn.setRequestProperty("SOAPAction", "");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(soapXml.getBytes(StandardCharsets.UTF_8));
        }
        // Capturamos la respuesta (incluso si es error 500)
        int status = conn.getResponseCode();
        InputStream inputStream = (status >= 400) ? conn.getErrorStream() : conn.getInputStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);

            if (status >= 400) {
                throw new RuntimeException("Respuesta de error de AFIP (HTTP " + status + "): " + sb.toString());
            }
            return sb.toString();
        }

    }}
