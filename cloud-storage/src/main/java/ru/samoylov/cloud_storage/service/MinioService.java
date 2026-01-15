package ru.samoylov.cloud_storage.service;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.samoylov.cloud_storage.dto.MinioDirectoryInfo;
import ru.samoylov.cloud_storage.dto.MinioResource;
import ru.samoylov.cloud_storage.dto.MinioResourceInfo;

import java.util.ArrayList;
import java.util.List;

@Service
public class MinioService {
    @Value("${minio.bucket-name}")
    private String bucketname;
    @Autowired
    private MinioClient minioClient;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public List<MinioResource> getInfoInFolder(String path) {


        List<MinioResource> minioResourceInfoList = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketname)
                            .prefix(path)
                            .recursive(false)
                            .build()
            );
            for (Result<Item> result : results) {
                Item item = result.get();
                String fileName = getFileNameWithoutPath(item.objectName());
                if (!item.isDir() && !item.objectName().endsWith("/")) {
                    MinioResourceInfo minioResourceInfo = new MinioResourceInfo();
                    minioResourceInfo.setSize(item.size());
                    // minioResourceInfo.setName(item.objectName());
                    minioResourceInfo.setName(fileName);
                    //minioResourceInfo.setType(item.isDir() ? "DIRECTORY" : "FILE");
                    minioResourceInfo.setType("FILE");
                    minioResourceInfo.setPath(path);
                    minioResourceInfoList.add(minioResourceInfo);
                    //.
                } else {
                    MinioDirectoryInfo minioDirectoryInfo = new MinioDirectoryInfo();
                    minioDirectoryInfo.setType("DIRECTORY");
                    minioDirectoryInfo.setPath(path);
                    minioDirectoryInfo.setName(item.objectName());
                    minioResourceInfoList.add(minioDirectoryInfo);
                }
            }
            // return minioResourceInfoList;
            return minioResourceInfoList;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения данных папки", e);
        }
    }


    public String getFileNameWithoutPath(String path) {
        int beginIndex = path.lastIndexOf('/')+1;
        String result = path.substring(beginIndex);
        return result;


    }
}
