package com.redepatas.api.services.impl;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.redepatas.api.services.IS3Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class S3ServiceImpl implements IS3Service {
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Autowired
    public S3ServiceImpl(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @Override
    public ResponseEntity<Map<String, String>> uploadFile(MultipartFile multipartFile, String folder) {
        try {
            String fileName = multipartFile.getOriginalFilename();
            String folderPrefix = "public/" + folder + "/";
            String initialFileKey = folderPrefix + fileName;

            // Check if the file already exists
            boolean fileExists = s3Client.listObjectsV2(builder -> builder
                    .bucket(bucketName)
                    .prefix(initialFileKey) // Use the initial file key here
                    .build())
                    .contents()
                    .stream()
                    .anyMatch(obj -> obj.key().equals(initialFileKey));

            String finalFileKey = initialFileKey; // Declare a final variable for the final file key

            if (fileExists) {
                String randomString = generateRandomString(8); 
                String fileExtension = "";
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex != -1) {
                    fileExtension = fileName.substring(dotIndex);
                    fileName = fileName.substring(0, dotIndex);
                }
                fileName = fileName + "_" + randomString + fileExtension;
                finalFileKey = folderPrefix + fileName;
            }

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(finalFileKey) // Use the final file key here
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));

            // Generate the URL of the uploaded object
            String fileUrl = getFileUrl(finalFileKey);

            // Create the response body
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("fileName", fileName);
            responseBody.put("fileUrl", fileUrl);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder randomString = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            randomString.append(characters.charAt(random.nextInt(characters.length())));
        }
        return randomString.toString();
    }

    @Override
    public ResponseEntity<String> deleteFile(String fileName) {
        try {
            String fileKey = "public/" + fileName;
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            return ResponseEntity.ok("File " + fileName + " deleted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting file");
        }
    }

    @Override
    public ResponseEntity<String> downloadFile(String fileUrl) {
        try {
            URL url = new URI(fileUrl).toURL();
            String fileKey = url.getPath().substring(1); // Remove a barra inicial

            // Gerar um URL pré-assinado para download
            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileKey)
                            .build())
                    .signatureDuration(Duration.ofMinutes(10))
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
            String presignedUrl = presignedGetObjectRequest.url().toString();

            return ResponseEntity.ok(presignedUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating presigned URL");
        }
    }


    private String getFileUrl(String fileKey) {
        GetUrlRequest request = GetUrlRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();
        return s3Client.utilities().getUrl(request).toExternalForm();
    }
}
