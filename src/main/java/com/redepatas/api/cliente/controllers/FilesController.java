package com.redepatas.api.cliente.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.redepatas.api.cliente.services.FileService;
import com.redepatas.api.cliente.services.IS3Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FilesController {

    @Autowired
    IS3Service is3Service;

    @Autowired
    private FileService fileService;

    @PostMapping("/uploadAvatar")
    public ResponseEntity<List<Map<String, String>>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        List<Map<String, String>> uploadedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                MultipartFile webpFile = fileService.convertToWebP(file);
                uploadedFiles.add(is3Service.uploadFile(webpFile, "users").getBody());
            } catch (IOException e) {
                throw new RuntimeException("Erro ao converter imagem: " + file.getOriginalFilename(), e);
            }
        }
        return ResponseEntity.ok(uploadedFiles);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam String fileName, @RequestParam UUID userId) {
        return is3Service.deleteFile(fileName);
    }

    @GetMapping("/download")
    public ResponseEntity<String> downloadFile(@RequestParam String fileUrl) {
        return is3Service.downloadFile(fileUrl);
    }

}
