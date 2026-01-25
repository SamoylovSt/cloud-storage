package ru.samoylov.cloud_storage.service;

import io.minio.*;
import io.minio.messages.Item;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.samoylov.cloud_storage.dto.MinioDirectoryInfo;
import ru.samoylov.cloud_storage.dto.MinioResource;
import ru.samoylov.cloud_storage.dto.MinioResourceInfo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    public String getRootFolder() {
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
        String rootFolder = getRootFolder();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketname)
                            .prefix(rootFolder + path)
                            .recursive(true)
                            .build()
            );
            for (Result<Item> result : results) {
                Item item = result.get();
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucketname)
                        .object(item.objectName())
                        .build());
            }
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

    public void downloadFile(String path) {
        String rootFolder = getRootFolder();
        try {
            minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketname)
                            .object(rootFolder + path)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки файла", e);
        }
    }

    public void download(String path, HttpServletResponse response) {
        if (isFolder(path)) {
            downloadFolder(path, response);
        } else {
            downloadFile(path);
        }
    }

    public void downloadFolder(String path, HttpServletResponse response) {
        String rootFolder = getRootFolder();
        String follPath = rootFolder + path;
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketname)
                .prefix(follPath)
                .recursive(true)
                .build());
        try {
            OutputStream servletOutputStream = response.getOutputStream();
            ZipOutputStream zout = new ZipOutputStream(servletOutputStream);
            for (Result<Item> result : results) {
                Item item = result.get();
                String objectKey = item.objectName();
                String keyForZipWithoutRootPath = objectKey.substring(rootFolder.length());
                if (objectKey.endsWith("/")) {
                    continue;
                }
                ZipEntry zipEntry = new ZipEntry(keyForZipWithoutRootPath);
                zout.putNextEntry(zipEntry);

                try (InputStream fileStream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucketname)
                                .object(objectKey)
                                .build()
                )) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fileStream.read(buffer)) != -1) {
                        zout.write(buffer, 0, bytesRead);
                    }
                }
            }
            zout.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean isFolder(String path) {
        String rootFolder = getRootFolder();
        String follPath = rootFolder + path;
        System.out.println(path + " путь из isFolder");
        if (path.endsWith("/")) {
            return true;
        }
        try {
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketname)
                    .object(follPath)
                    .build()
            );

            if (stat != null && stat.size() > 0) {
                return false;
            }
            String prefix = follPath.endsWith("/") ? follPath : follPath + "/";

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketname)
                            .prefix(prefix)
                            .maxKeys(1)
                            .build()
            );

            return results.iterator().hasNext();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MinioResource upload(String path, MultipartFile file) {
        try {
            String rootFolder = getRootFolder();
            String fileName = getFileNameWithoutPath(path);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketname)
                            .object(rootFolder + path)
                            .stream(
                                    file.getInputStream(),
                                    file.getSize(),
                                    -1
                            )
                            .contentType(file.getContentType())
                            .build()
            );
            MinioResourceInfo minioResourceInfo = new MinioResourceInfo();
            minioResourceInfo.setSize(file.getSize());
            minioResourceInfo.setType("FILE");
            minioResourceInfo.setPath(rootFolder + path);
            minioResourceInfo.setName(fileName);
            return minioResourceInfo;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке ресурса", e);
        }
    }

    public List<MinioResource> uploadMultiple(String path, MultipartFile[] files) {
        List<MinioResource> results = new ArrayList<>();
        for (MultipartFile file : files) {
            String fullPath = path + file.getOriginalFilename();
            MinioResource info = upload(fullPath, file);
            results.add(info);
        }
        return results;
    }

    public List<MinioResource> search(String query) {
        List<MinioResource> minioResourceList = new ArrayList<>();
        String rootFolder = getRootFolder();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketname)
                    .prefix(rootFolder)
                    .recursive(true)
                    .build());

            for (Result<Item> result : results) {

                Item item = result.get();
                String path = item.objectName();
                System.out.println("путь с включением " + path);
                if (path.contains(query + "/")) {

                    String searchPattern = query + "/";
                    int indexForSubstring = path.indexOf(searchPattern);

                    String finalPath = "";
                    if (indexForSubstring != -1) {
                        finalPath = path.substring(0, indexForSubstring + searchPattern.length());
                        finalPath = finalPath.replace(rootFolder, "");
                        finalPath=finalPath.replace(searchPattern,"");
                        System.out.println("финальный путь " + finalPath);
                    }

                    MinioDirectoryInfo minioDirectoryInfo = new MinioDirectoryInfo();
                    minioDirectoryInfo.setType("DIRECTORY");
                    minioDirectoryInfo.setPath(finalPath);
                    minioDirectoryInfo.setName(query + "/");
                    minioResourceList.add(minioDirectoryInfo);
                    break;
                }

            }
            return minioResourceList;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
