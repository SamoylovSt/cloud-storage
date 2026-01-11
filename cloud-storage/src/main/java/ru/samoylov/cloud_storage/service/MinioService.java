package ru.samoylov.cloud_storage.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinioService {

    private MinioClient minioClient;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
        createBucketIfNotExist();
    }

    @Value("${minio.bucket-name}")
    private String bucketname;

    public void createFolder(String folderName) {

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketname)
                    .object("gavno")
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createBucketIfNotExist() {
        try {
            boolean exist = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketname)
                    .build());

            if (!exist) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketname)
                                .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
