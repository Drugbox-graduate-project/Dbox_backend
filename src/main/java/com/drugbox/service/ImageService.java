package com.drugbox.service;

import com.drugbox.common.exception.CustomException;
import com.drugbox.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.StringUtils;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ImageService {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    private final S3Client s3Client;

    public String uploadImage(MultipartFile image) throws IOException {
        String uuid = UUID.randomUUID().toString(); // AWS S3 Storage에 저장될 파일 이름
        String ext = image.getContentType(); // 불가: application/octet-stream(jfif파일), 가능: image/jpeg, image/png 등
        if(!ext.contains("image")){
            throw new CustomException(ErrorCode.IMAGE_TYPE_INVALID);
        }

        return this.putS3(image, uuid);
    }

    private RequestBody getFileRequestBody(MultipartFile file) throws IOException {
        return RequestBody.fromInputStream(file.getInputStream(), file.getSize());
    }

    // 실제 업로드 하는 메소드
    private String putS3(MultipartFile file, String uuid) throws IOException {
        String ext = file.getContentType();
        PutObjectRequest objectRequest =PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uuid)
                .contentType(ext)
                .contentLength(file.getSize())
                .build();

        RequestBody rb = getFileRequestBody(file);
        s3Client.putObject(objectRequest, rb);
        return uuid;
    }

    public String processImage(String image) {
        if (StringUtils.isBlank(image)) {
            return null;
        }
        if (image.startsWith("https://")) { // 구글 프로필 이미지 처리
            return image;
        }
        return "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/" + image;
    }
}
