package com.redepatas.api.services;



import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface IS3Service {

    public ResponseEntity<Map<String, String>> uploadFile(MultipartFile multipartFilel, String folder);

    public ResponseEntity<String> deleteFile(String fileName);

    public ResponseEntity<String> downloadFile(String fileUrl);
}
