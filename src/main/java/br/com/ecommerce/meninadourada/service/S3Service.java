package br.com.ecommerce.meninadourada.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile; // Importe para MultipartFile
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

/**
 * Serviço para interagir com o Amazon S3 para upload e exclusão de arquivos.
 */
@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Autowired
    public S3Service(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Realiza o upload de um arquivo para o Amazon S3.
     * O arquivo é salvo com um nome único (UUID) para evitar colisões.
     *
     * @param file O arquivo a ser enviado (MultipartFile).
     * @return A URL pública do file no S3.
     * @throws IOException Se houver um erro ao ler o InputStream do file.
     * @throws RuntimeException Se houver um erro no upload para o S3.
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("\\s+", "_");

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName, fileName, file.getInputStream(), metadata);

            // REMOVIDO: putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead); // Esta linha foi removida

            s3Client.putObject(putObjectRequest);
            String fileUrl = s3Client.getUrl(bucketName, fileName).toString();
            logger.info("File {} sent to S3 successfully. URL: {}", originalFilename, fileUrl);
            return fileUrl;
        } catch (IOException e) {
            logger.error("IO error reading file for S3 upload: {}", e.getMessage(), e);
            throw new IOException("Error reading file for S3 upload: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error uploading file {} to S3: {}", originalFilename, e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a file from Amazon S3.
     *
     * @param fileUrl The public URL of the file in S3 to be deleted.
     * @throws RuntimeException If there is an error deleting the file.
     */
    public void deleteFile(String fileUrl) {
        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            s3Client.deleteObject(bucketName, fileName);
            logger.info("File {} deleted from S3 successfully.", fileName);
        } catch (Exception e) {
            logger.error("Error deleting file {} from S3: {}", fileUrl, e.getMessage(), e);
            throw new RuntimeException("Failed to delete file from S3: " + e.getMessage(), e);
        }
    }
}
