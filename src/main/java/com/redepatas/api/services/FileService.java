package com.redepatas.api.services;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

@Service
public class FileService {

    public MultipartFile convertToWebP(MultipartFile originalFile) throws IOException {
        BufferedImage image = ImageIO.read(originalFile.getInputStream());
        if (image == null) {
            throw new IOException("Não foi possível ler a imagem.");
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(os);

        // Obtém o writer para WebP usando a biblioteca WebP ImageIO
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("webp");
        if (!writers.hasNext()) {
            throw new IOException("Não há escritor para o formato WebP.");
        }
        ImageWriter writer = writers.next();
        writer.setOutput(ios);

        // Escreve a imagem no formato WebP
        writer.write(null, new IIOImage(image, null, null), null);
        writer.dispose();
        ios.close();

        // Converte para MultipartFile novamente
        return new MultipartFile() {
            @Override
            @NonNull 
            public String getName() {
                return originalFile.getName();
            }

            @Override
            public String getOriginalFilename() {
                return originalFile.getOriginalFilename().replaceAll("\\.[^.]+$", "") + ".webp";
            }

            @Override
            public String getContentType() {
                return "image/webp";
            }

            @Override
            public boolean isEmpty() {
                return os.size() == 0;
            }

            @Override
            public long getSize() {
                return os.size();
            }

            @NonNull 
            @Override
            public byte[] getBytes() {
                return os.toByteArray();
            }

            @Override
            @NonNull 
            public InputStream getInputStream() {
                return new ByteArrayInputStream(os.toByteArray());
            }

            @Override
            public void transferTo(@NonNull File dest) throws IOException, IllegalStateException {
                try (FileOutputStream fos = new FileOutputStream(dest)) {
                    fos.write(os.toByteArray());
                }
            }
        };
    }
}
