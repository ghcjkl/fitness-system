package com.fitness.fitness.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class UploadFileUtil {

    // 本地存储根目录，可自行修改
    private static final String BASE_PATH = "D:/fitness_upload/";

    /**
     * 上传图片文件
     * @param file 前端上传文件
     * @return 文件访问路径
     */
    public String uploadImg(MultipartFile file) throws IOException {
        // 按日期分文件夹存储
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dirPath = BASE_PATH + dateDir + "/";
        FileUtil.mkdir(dirPath);

        // 获取后缀
        String originalName = file.getOriginalFilename();
        String suffix = originalName.substring(originalName.lastIndexOf("."));
        // 唯一文件名
        String fileName = IdUtil.simpleUUID() + suffix;

        // 保存文件
        File saveFile = new File(dirPath + fileName);
        file.transferTo(saveFile);

        // 返回相对访问路径
        return "/upload/" + dateDir + "/" + fileName;
    }
}