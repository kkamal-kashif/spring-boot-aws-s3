package com.kk.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;

import lombok.SneakyThrows;

@Service
public class FileServiceImpl implements FileService {

    Logger logger = LogManager.getLogger(this.getClass().getName());
    @Value("${bucketName}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3;

    @Override
    @SneakyThrows
    public String saveFile(MultipartFile file) {
        File f = convertMultiPartToFile(file);
        PutObjectResult putObjectResult = s3.putObject(bucketName, file.getOriginalFilename(), f);
        return putObjectResult.getContentMd5();
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File f = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(file.getBytes());
        fos.close();
        return f;
    }

    @SneakyThrows
    @Override
    public String downloadFile(String fileName) {
        logger.info("File to be fetched from S3 {}", fileName);
        S3Object s3Object = s3.getObject(bucketName, fileName);
        InputStream objectContent = s3Object.getObjectContent();
        String content = IOUtils.toString(objectContent);
        logger.info("Content {}", content);
        return content;
    }

    @Override
    public String createBucket(String bucketName) {
        try {
            if (bucketAlreadyExists(bucketName)) {
                logger.error("bucket already exist");
                throw new AmazonS3Exception("bucket already exist");
            }
            s3.createBucket(bucketName);
        } catch (AmazonS3Exception s3Exception) {
            logger.error("Unable to create bucket :" + s3Exception.getMessage());
        }
        return "Bucket created with name:" + bucketName;
    }

    private boolean bucketAlreadyExists(String bucketName) {
        logger.info("Inside method bucketAlreadyExists");
        return s3.doesBucketExistV2(bucketName);
    }

    @Override
    public String uploadFile(MultipartFile multiPart, String bucketName) {
        if (multiPart.isEmpty()) {
            throw new IllegalStateException("Cannot upload empty file");
        }
        PutObjectResult putObjectResult = null;
        try {
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + multiPart.getOriginalFilename());
            multiPart.transferTo(convFile);
            putObjectResult = s3.putObject(bucketName, convFile.getName(), convFile);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to upload file", e);
        }
        return "File Uploaded Successfully - " + putObjectResult.getContentMd5();
    }

    @Override
    public String deleteFile(String bucketName, String fileName) {
        s3.deleteObject(bucketName, fileName);
        return "File deleted successfully";
    }

    @Override
    public String deleteBucket(String bucketName) {
        s3.deleteBucket(bucketName);
        return "Bucket deleted successfully";
    }

}
