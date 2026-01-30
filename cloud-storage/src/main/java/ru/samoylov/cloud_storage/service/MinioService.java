package ru.samoylov.cloud_storage.service;

import io.minio.*;
import io.minio.messages.Item;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.samoylov.cloud_storage.dto.MinioResourceInfo;
import ru.samoylov.cloud_storage.exception.AppException;

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

    public List<MinioResourceInfo> getInfoInFolder(String path) {
        List<MinioResourceInfo> minioResourceInfoList = new ArrayList<>();
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
                String folderName = geObjectNameWithoutPath(item.objectName());
                if (!item.isDir() && item.size() != 0) {
                    MinioResourceInfo minioResourceInfo = new MinioResourceInfo();
                    minioResourceInfo.setSize(item.size());
                    minioResourceInfo.setName(fileName);
                    minioResourceInfo.setType("FILE");
                    minioResourceInfo.setPath(path);
                    minioResourceInfoList.add(minioResourceInfo);
                } else if (item.isDir()) {
                    MinioResourceInfo minioDirectoryInfo = new MinioResourceInfo();
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

    public String geObjectNameWithoutPath(String path) {
        String[] splitList = path.split("/");
       if(splitList.length>0){
           String result = splitList[splitList.length - 1];

           if (result.endsWith("/")) {
               result = result.substring(0, result.length() - 1);
           }
           return result;
       }else {
           return path;
       }
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

    public MinioResourceInfo createFolder(String path) {
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
            MinioResourceInfo minioDirectoryInfo = new MinioResourceInfo();
            minioDirectoryInfo.setType("DIRECTORY");
            minioDirectoryInfo.setName(geObjectNameWithoutPath(path));
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

    public MinioResourceInfo getResourceInfo(String path) {
        String rootFolder = getRootFolder();
        try {
            if (!path.contains(rootFolder)) {
                path = rootFolder + path;
            }
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketname)
                            .object(path)
                            .build()
            );

            if (path.endsWith("/")) {
                String folderName = geObjectNameWithoutPath(path)+"/";
                path = path.replace(rootFolder, "");
                path = path.replace(folderName, "");

                MinioResourceInfo minioDirectoryInfo = new MinioResourceInfo();
                minioDirectoryInfo.setType("DIRECTORY");
                minioDirectoryInfo.setPath(path);
                minioDirectoryInfo.setName(folderName);
                return minioDirectoryInfo;
            } else {
                String fileName = geObjectNameWithoutPath(path);
                path = path.replace(rootFolder, "");
                path = path.replace(fileName, "");
                MinioResourceInfo minioResourceInfo = new MinioResourceInfo();
                minioResourceInfo.setName(fileName);
                minioResourceInfo.setType("FILE");
                minioResourceInfo.setPath(path);
                minioResourceInfo.setSize(stat.size());
                return minioResourceInfo;
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения информации о ресурсе", e);
        }
    }

    public void downloadFile(String path, HttpServletResponse response) {
        String rootFolder = getRootFolder();
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketname)
                        .object(rootFolder + path)
                        .build()
        );
             OutputStream outputStream = response.getOutputStream();) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки файла", e);
        }
    }

    public void download(String path, HttpServletResponse response) {
        if (isFolder(path)) {
            downloadFolder(path, response);
        } else {
            downloadFile(path, response);
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

    public MinioResourceInfo upload(String path, MultipartFile file) {
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

    public List<MinioResourceInfo> uploadMultiple(String path, MultipartFile[] files) {
        List<MinioResourceInfo> results = new ArrayList<>();
        for (MultipartFile file : files) {
            String fullPath = path + file.getOriginalFilename();
            MinioResourceInfo info = upload(fullPath, file);
            results.add(info);
        }
        return results;
    }

    public List<MinioResourceInfo> search(String query) {
        List<MinioResourceInfo> minioResourceList = new ArrayList<>();
        String rootFolder = getRootFolder();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketname)
                    .prefix(rootFolder)
                    .delimiter("/")
                    .recursive(true)
                    .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                String path = item.objectName();

                String searchPattern = query + "/";
                String objectName = geObjectNameWithoutPath(path);

                String pathForFolder = path.replace(rootFolder, "");
                pathForFolder = pathForFolder.replace(objectName + "/", "");
                pathForFolder = pathForFolder.replace(searchPattern, "");

                if (path.contains(query) && path.endsWith(objectName + "/") && objectName.contains(query)) {

                    MinioResourceInfo minioDirectoryInfo = new MinioResourceInfo();

                    minioDirectoryInfo.setType("DIRECTORY");
                    minioDirectoryInfo.setPath(pathForFolder);
                    minioDirectoryInfo.setName(objectName + "/");
                    minioResourceList.add(minioDirectoryInfo);

                } else if (item.size() > 0 && objectName.contains(query)) {

                    String finalPath = path.replace(rootFolder, "");
                    finalPath = finalPath.replace(objectName, "");
                    MinioResourceInfo minioResourceInfo = new MinioResourceInfo();
                    minioResourceInfo.setSize(item.size());
                    minioResourceInfo.setName(objectName);
                    minioResourceInfo.setType("FILE");
                    minioResourceInfo.setPath(finalPath);
                    minioResourceList.add(minioResourceInfo);
                }
            }
            return minioResourceList;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MinioResourceInfo renameFolder(String from, String to) {
        String rootFolder = getRootFolder();
        String sourcePath = rootFolder + from;
        try {
            if (!sourcePath.endsWith("/")) {
                sourcePath += "/";
            }

            String targetPath = rootFolder + to;
            if (!targetPath.endsWith("/")) {
                targetPath += "/";
            }

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketname)
                            .prefix(sourcePath)
                            .recursive(true)
                            .build()
            );

            List<String> objectsToProcess = new ArrayList<>();
            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();

                if (!objectName.equals(sourcePath) && !objectName.equals(sourcePath.substring(0, sourcePath.length() - 1))) {
                    objectsToProcess.add(objectName);
                }
            }
            if (objectsToProcess.size() == 0 || objectsToProcess.isEmpty()) {
                renameSingleObject(from, to);
                return getResourceInfo(to);
            }
            for (String sourceObjectName : objectsToProcess) {
                String destinationObjectName = sourceObjectName.replace(
                        sourcePath,
                        targetPath
                );

                minioClient.copyObject(CopyObjectArgs.builder()
                        .bucket(bucketname)
                        .object(destinationObjectName)
                        .source(CopySource.builder()
                                .bucket(bucketname)
                                .object(sourceObjectName)
                                .build())
                        .build());
            }
            for (String sourceObjectName : objectsToProcess) {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucketname)
                        .object(sourceObjectName)
                        .build());
            }

            System.out.println("что идёт в гетресурс"+ to);
            return getResourceInfo(to);
        } catch (Exception e) {
            throw new RuntimeException("ошибка перемещения/переименования ресурса", e);
        }

    }

    public void renameSingleObject(String from, String to) {
        String rootFolder = getRootFolder();
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucketname)
                    .object(rootFolder + to)
                    .source(CopySource.builder()
                            .bucket(bucketname)
                            .object(rootFolder + from)
                            .build())
                    .build());
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketname)
                    .object(rootFolder + from)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка переименования/перемещения одиночного ресурса", e);
        }


    }

    public MinioResourceInfo rename(String from, String to) {
        try {
            if (from.endsWith("/")) {
                return renameFolder(from, to);
            }
            renameSingleObject(from, to);
            return getResourceInfo(to);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка перемещения/переименования", e);
        }
    }

}
