package org.simior.strategy;

import org.springframework.web.multipart.MultipartFile;

/**
 * OSS文件上传策略接口
 */
public interface UploadStrategy {

    /**
     * 上传文件
     *
     * @param file 文件
     * @param path 路径
     * @return 文件访问地址
     */
    String uploadFile(MultipartFile file, String path);

    /**
     * 删除文件
     *
     * @param fileUrl 文件地址
     */
    void deleteFile(String fileUrl);
}
