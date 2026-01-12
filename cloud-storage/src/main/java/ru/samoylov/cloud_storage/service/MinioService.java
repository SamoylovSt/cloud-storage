package ru.samoylov.cloud_storage.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.samoylov.cloud_storage.dto.MinioResourceInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MinioService {
    @Value("${minio.bucket-name}")
    private String bucketname;
    @Autowired
    private MinioClient minioClient;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
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

    public MinioResourceInfo getResourceInfo(String file) {
        createBucketIfNotExist();
     //   file=extractFileName(file);
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketname)
                            .object(file)
                            .build()
            );
            MinioResourceInfo resourceInfo = new MinioResourceInfo();
            resourceInfo.setPath(file);
            resourceInfo.setName(extractFileName(file));
            resourceInfo.setSize(stat.size());
            //  resourceInfo.setType(stat.contentType());
            resourceInfo.setType("FILE");
            return resourceInfo;
        } catch (MinioException e) {
            throw new RuntimeException("MinIO get info error " + file + e);
        } catch (Exception e) {
            throw new RuntimeException("Unknown error when working with MinIO", e);
        }
    }

    public void deleteResource(String filePath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketname)
                            .object(filePath)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error when deleting a file from MinIO: " + filePath, e);
        }
    }

    private String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return filePath;
        }
        int lastSlash = filePath.lastIndexOf('/');
        String filename;
        if (lastSlash >= 0 && lastSlash < filePath.length() - 1) {
            filename =filePath.substring(lastSlash + 1);
        } else {
             filename =filePath;
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastSlash > 0) {
            return filename.substring(0, lastDotIndex);
        }
        return filename;
    }

    public void createFolder(String folderName) {
        createBucketIfNotExist();
        if (!folderName.endsWith("/")) {
            folderName = folderName + "/";
        }
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketname)
                    .object(folderName )
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .headers(Map.of("Content-Type", "application/x-directory"))
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public String extractFolderName(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return "";
        }
        String trimmed = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int lastSlash = trimmed.lastIndexOf('/');
        if (lastSlash >= 0) {
            return trimmed.substring(lastSlash + 1);
        }
        return trimmed;
    }

    public List<MinioResourceInfo> getResourceInFolder(String folderPath) {
        List<MinioResourceInfo> resources = new ArrayList<>();
        try {

            String prefix = folderPath.endsWith("/") ? folderPath : folderPath + "/";
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketname)
                            .prefix(prefix)//тут хз
                            .recursive(false)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();

                MinioResourceInfo resourceInfo = new MinioResourceInfo();
                resourceInfo.setPath(item.objectName());
                resourceInfo.setName(extractFileName(item.objectName()));
                resourceInfo.setSize(item.size());
                resourceInfo.setType(item.isDir() ? "DIRECTORY" : "FILE");

                resources.add(resourceInfo);
            }

            return resources;
        } catch (Exception e) {
            throw new RuntimeException();
        }

    }

}
