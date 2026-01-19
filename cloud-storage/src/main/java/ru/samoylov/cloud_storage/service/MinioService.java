package ru.samoylov.cloud_storage.service;

import io.minio.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.samoylov.cloud_storage.dto.MinioDirectoryInfo;
import ru.samoylov.cloud_storage.dto.MinioResource;
import ru.samoylov.cloud_storage.dto.MinioResourceInfo;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class MinioService {
    @Autowired
    private UserService userService;
    @Value("${minio.bucket-name}")
    private String bucketname;
    @Autowired
    private MinioClient minioClient;


    public MinioService(MinioClient minioClient, UserService userService) {
        this.minioClient = minioClient;
        this.userService = userService;
    }

    private String getRootFolder() {
        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName().toString();
        long currentUserId = userService.getCurrentUserIdByName(currentUserName);
        String rootFolder = "user-" + currentUserId + "-files/";
        return rootFolder;
    }


    public List<MinioResource> getInfoInFolder(String path) {
        List<MinioResource> minioResourceInfoList = new ArrayList<>();
        String rootFolder = getRootFolder();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketname)
                            .prefix(rootFolder + path)
                            .recursive(false)
                            .build()
            );
            for (Result<Item> result : results) {
                Item item = result.get();
                String fileName = getFileNameWithoutPath(item.objectName());
                String folderName = getFolderNameWithoutPath(item.objectName());
                System.out.println(folderName + " имя папки без пути");
                if (!item.isDir() && item.size() != 0) {
                    MinioResourceInfo minioResourceInfo = new MinioResourceInfo();
                    minioResourceInfo.setSize(item.size());
                    minioResourceInfo.setName(fileName);
                    minioResourceInfo.setType("FILE");
                    minioResourceInfo.setPath(path);
                    minioResourceInfoList.add(minioResourceInfo);
                } else if (item.isDir()) {
                    MinioDirectoryInfo minioDirectoryInfo = new MinioDirectoryInfo();
                    minioDirectoryInfo.setType("DIRECTORY");
                    minioDirectoryInfo.setPath(path);
                    minioDirectoryInfo.setName(folderName + "/");
                    //TODO имя отображается не по тз со /  без "/" в имени, папки отображаются как файлы
                    minioResourceInfoList.add(minioDirectoryInfo);
                }
            }
            return minioResourceInfoList;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения данных папки", e);
        }
    }

    public String getFileNameWithoutPath(String path) {
        int beginIndex = path.lastIndexOf('/') + 1;
        String result = path.substring(beginIndex);
        return result;
    }

    public String getFolderNameWithoutPath(String path) {
        String[] splitList = path.split("/");
        String result = splitList[splitList.length - 1];

        if (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;

    }

    private String normalizePath(String path) {
        String rootFolder = getRootFolder();
        if (path == null || path.trim().isEmpty()) {
            return rootFolder;
        }
        if (!path.contains(rootFolder)) {
            path = rootFolder + path;
        }
        path = path.trim();
        path = path.replaceAll("/+", "/");

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public MinioDirectoryInfo createFolder(String path) {
        path = normalizePath(path);
        String folderPathForSave = path.endsWith("/") ? path : path + "/";
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketname)
                            .object(folderPathForSave)
                            .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build()
            );
            MinioDirectoryInfo minioDirectoryInfo = new MinioDirectoryInfo();
            minioDirectoryInfo.setType("DIRECTORY");
            minioDirectoryInfo.setName(getFolderNameWithoutPath(path));
            minioDirectoryInfo.setPath(folderPathForSave);
            return minioDirectoryInfo;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании папки", e);
        }
    }

    public void deleteResource(String path) {
      String rootFolder=getRootFolder();
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketname)
                            .object(rootFolder+path)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("ошибка удаления ресурса", e);
        }
    }

    public MinioResource getResourceInfo(String path) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketname)
                            .object(path)
                            .build()
            );
            if (path.endsWith("/")) {
                MinioDirectoryInfo minioDirectoryInfo = new MinioDirectoryInfo();
                minioDirectoryInfo.setType("DIRECTORY");
                minioDirectoryInfo.setPath(path);
                minioDirectoryInfo.setName(getFolderNameWithoutPath(path));
                return minioDirectoryInfo;
            } else {
                MinioResourceInfo minioResourceInfo = new MinioResourceInfo();
                minioResourceInfo.setName(getFileNameWithoutPath(path));
                minioResourceInfo.setType("FILE");
                minioResourceInfo.setPath(path);
                minioResourceInfo.setSize(stat.size());
                return minioResourceInfo;
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения информации о ресурсе", e);
        }

    }


    public MinioResourceInfo uploadResource(String path, MultipartFile file) {
        try {
            String rootFolder=getRootFolder();
            path = normalizePath(path);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketname)
                            .object(path+"/")
                            .stream(
                                    file.getInputStream(),  // InputStream из MultipartFile
                                    file.getSize(),         // Размер файла
                                    -1                      // Часть размера (не используется)
                            )
                            .contentType(file.getContentType())
                            .build()
            );
            MinioResourceInfo minioResourceInfo = new MinioResourceInfo();
            minioResourceInfo.setPath(path);
            long l = 1;
            minioResourceInfo.setSize(l);
            minioResourceInfo.setType(getResourceInfo(path).getType());
            minioResourceInfo.setName(getResourceInfo(path).getName());
            return minioResourceInfo;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке ресурса", e);
        }
    }

}
