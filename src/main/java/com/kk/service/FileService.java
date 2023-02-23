package com.kk.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

	String createBucket(String bucketName);

	String uploadFile(MultipartFile file, String bucketName);

	String deleteFile(String bucketName, String fileName);

	public String downloadFile(String fileName);

	String deleteBucket(String bucketName);

	String saveFile(MultipartFile file);

}
