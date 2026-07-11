package org.simior.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.io.FileTypeUtil;
import lombok.RequiredArgsConstructor;
import org.simior.common.result.Result;
import org.simior.properties.UploadProperties;
import org.simior.strategy.UploadStrategy;
import org.simior.strategy.UploadStrategyFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.file.Path;

/**
 * 文件上传控制器
 * <p>
 * 提供图片和文章图片的上传及文件删除功能。
 * URL 前缀：/v1/files，所有接口需要登录。
 */
@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final UploadStrategyFactory uploadStrategyFactory;
    private final UploadProperties uploadProperties;

    /**
     * 上传图片（通用，5MB 限制）
     *
     * @param file 图片文件
     * @return 文件访问地址
     */
    @SaCheckLogin
    @PostMapping("/images")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return doUpload(file, "images", 5);
    }

    /**
     * 上传文章图片（10MB 限制）
     *
     * @param file 图片文件
     * @return 文件访问地址
     */
    @SaCheckLogin
    @PostMapping("/articles")
    public Result<String> uploadArticleImage(@RequestParam("file") MultipartFile file) {
        return doUpload(file, "articles", 10);
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件地址
     * @return 删除结果
     */
    @SaCheckRole("admin")
    @DeleteMapping
    public Result<String> deleteFile(@RequestParam String fileUrl) {
        if (!isValidFileUrl(fileUrl)) {
            return Result.error("无效的文件地址");
        }
        if (fileUrl.contains("..")) {
            return Result.error("无效的文件地址");
        }
        UploadStrategy uploadStrategy = uploadStrategyFactory.getStrategy();
        uploadStrategy.deleteFile(fileUrl);
        return Result.success("删除成功");
    }

    /**
     * 通用文件上传逻辑
     *
     * @param file      上传文件
     * @param directory 存储子目录
     * @param maxMB     最大文件大小（MB）
     * @return 上传结果
     */
    private Result<String> doUpload(MultipartFile file, String directory, int maxMB) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只支持图片格式");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return Result.error("文件名不能为空");
        }
        // 清理文件名：去除路径分隔符和特殊字符，防止路径注入
        String safeName = originalFilename.replaceAll(".*[/\\\\]", "");
        if (safeName.isBlank()) {
            return Result.error("文件名不合法");
        }
        String lowerName = safeName.toLowerCase();
        if (!lowerName.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp|ico)$")) {
            return Result.error("不支持的图片格式");
        }
        // 校验 Content-Type 与扩展名一致性
        String ext = lowerName.substring(lowerName.lastIndexOf('.') + 1);
        if (!isContentTypeMatchExtension(contentType, ext)) {
            return Result.error("文件内容与扩展名不匹配");
        }
        // Magic Number 校验：读取文件头字节判断真实类型，防止伪造扩展名和 Content-Type
        String realType;
        try {
            realType = FileTypeUtil.getType(file.getInputStream());
        } catch (Exception e) {
            return Result.error("文件读取失败");
        }
        if (!isAllowedImageType(realType)) {
            return Result.error("文件头校验失败：非合法图片文件");
        }
        long maxSize = (long) maxMB * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return Result.error("文件大小不能超过" + maxMB + "MB");
        }
        UploadStrategy uploadStrategy = uploadStrategyFactory.getStrategy();
        String fileUrl = uploadStrategy.uploadFile(file, directory);
        return Result.success("上传成功", fileUrl);
    }

    /**
     * 校验 Content-Type 与文件扩展名是否一致
     */
    private boolean isContentTypeMatchExtension(String contentType, String ext) {
        return switch (ext) {
            case "jpg", "jpeg" -> contentType.equals("image/jpeg");
            case "png" -> contentType.equals("image/png");
            case "gif" -> contentType.equals("image/gif");
            case "bmp" -> contentType.equals("image/bmp");
            case "webp" -> contentType.equals("image/webp");
            case "ico" -> contentType.equals("image/x-icon") || contentType.equals("image/vnd.microsoft.icon");
            default -> false;
        };
    }

    /**
     * 校验 Magic Number 识别出的真实文件类型是否为允许的图片格式
     */
    private boolean isAllowedImageType(String realType) {
        if (realType == null) return false;
        return switch (realType.toLowerCase()) {
            case "jpg", "jpeg", "png", "gif", "bmp", "webp", "ico" -> true;
            default -> false;
        };
    }

    /**
     * 校验文件 URL 是否属于当前配置的存储服务
     * 使用 URL 标准化防止编码绕过攻击
     *
     * @param fileUrl 待校验的文件 URL
     * @return 如果 URL 有效且属于当前存储服务返回 {@code true}，否则返回 {@code false}
     */
    private boolean isValidFileUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return false;
        }
        // 标准化 URL，防止编码绕过（如 ..%2F 或 %2e%2e/）
        String normalizedUrl;
        try {
            normalizedUrl = URI.create(fileUrl).normalize().toString();
        } catch (IllegalArgumentException e) {
            return false;
        }
        // 双重检查：标准化后的 URL 不应包含路径遍历
        if (normalizedUrl.contains("..")) {
            return false;
        }
        String mode = uploadProperties.getMode();
        if ("minio".equals(mode)) {
            String expectedPrefix = uploadProperties.getMinio().getEndpoint() + "/"
                    + uploadProperties.getMinio().getBucketName() + "/";
            return normalizedUrl.startsWith(expectedPrefix);
        } else if ("oss".equals(mode)) {
            String expectedHost = "https://" + uploadProperties.getOss().getBucketName()
                    + "." + uploadProperties.getOss().getEndpoint() + "/";
            return normalizedUrl.startsWith(expectedHost);
        } else if ("local".equals(mode)) {
            return normalizedUrl.startsWith("/uploads/");
        }
        return false;
    }
}
