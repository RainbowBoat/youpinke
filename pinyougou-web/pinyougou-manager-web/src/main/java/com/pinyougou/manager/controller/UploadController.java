package com.pinyougou.manager.controller;

import org.apache.commons.io.FilenameUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UploadController {

    @Value("${fileServerUrl}")
    private String fileServerUrl;

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile multipartFile) {

        Map<String, Object> data = new HashMap<>();
        data.put("status", 500);

        try {
            String conf_filename = this.getClass().getResource("/fastdfs_client.conf.conf").getPath();

            ClientGlobal.init(conf_filename);

            StorageClient storageClient = new StorageClient();

            String originalName = multipartFile.getOriginalFilename();

            String[] arr = storageClient.upload_file(multipartFile.getBytes(), FilenameUtils.getExtension(originalName), null);

            StringBuilder sb = new StringBuilder(fileServerUrl);

            for (String s : arr) {
                sb.append("/");
                sb.append(s);
            }

            data.put("status", 200);

            data.put("url", sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
}
