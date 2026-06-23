package org.simior.strategy.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simior.common.exception.BusinessException;
import org.simior.properties.UploadProperties;
import org.simior.strategy.UploadStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * MinIO上传策略实现
 *
 */
@Slf4j
@Service("minioUploadStrategyImpl")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "blog.upload.mode", havingValue = "minio")
public class MinioUploadStrategyImpl implements UploadStrategy {

    private final MinioClient minioClient;
    private final UploadProperties uploadProperties;

    @Override
    public String uploadFile(MultipartFile file, String path) {
        try {
            // 获取文件原名
            String originalFilename = file.getOriginalFilename();
            // 获取文件后缀
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 生成新文件名：日期/UUID.后缀
            String fileName = DateUtil.today() + "/" + IdUtil.fastSimpleUUID() + suffix;
            // 拼接完整路径
            String objectName = path + "/" + fileName;

            // 上传文件
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(uploadProperties.getMinio().getBucketName())
                                .object(objectName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            // 返回文件访问路径
            return uploadProperties.getMinio().getEndpoint() + "/" +
                    uploadProperties.getMinio().getBucketName() + "/" + objectName;

        } catch (Exception e) {
            log.error("MinIO文件上传失败：{}", e.getMessage(), e);
            throw new BusinessException("文件上传失败");
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // 从URL中提取对象名称
            String objectName = fileUrl.substring(fileUrl.indexOf(uploadProperties.getMinio().getBucketName())
                    + uploadProperties.getMinio().getBucketName().length() + 1);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(uploadProperties.getMinio().getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO文件删除失败：{}", e.getMessage(), e);
            throw new BusinessException("文件删除失败");
        }
    }
}
