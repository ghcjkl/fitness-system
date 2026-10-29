package com.fitness.fitness.serviceimpl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.fitness.entity.MedicalReport;
import com.fitness.fitness.exception.BusinessException;
import com.fitness.fitness.mapper.MedicalReportMapper;
import com.fitness.fitness.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class ReportServiceImpl implements ReportService {

    private static final String BASE_PATH = "D:/fitness_upload/";
    private static final long MAX_SIZE = 10 * 1024 * 1024L;
    private static final Set<String> ALLOWED_TYPES = Set.of("pdf", "jpg", "jpeg", "png");

    @Autowired
    private MedicalReportMapper reportMapper;

    @Override
    public MedicalReport upload(MultipartFile file, LocalDate reportDate, Long userId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的体检报告");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new BusinessException("文件格式不支持");
        }
        String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_TYPES.contains(ext)) {
            throw new BusinessException("仅支持 PDF、JPG、JPEG、PNG 格式");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("文件不能超过 10MB");
        }

        // 按用户分目录存储，避免互相覆盖
        String relativeDir = "report/" + userId + "/";
        FileUtil.mkdir(BASE_PATH + relativeDir);
        String fileName = IdUtil.simpleUUID() + "." + ext;
        file.transferTo(new File(BASE_PATH + relativeDir + fileName));

        MedicalReport report = new MedicalReport();
        report.setUserId(userId);
        report.setFileName(originalName);
        report.setFilePath(relativeDir + fileName);
        report.setFileType(ext);
        report.setFileSize(file.getSize());
        report.setReportDate(reportDate);
        report.setStatus("DONE");
        report.setCreateTime(LocalDateTime.now());
        reportMapper.insert(report);
        return report;
    }

    @Override
    public List<MedicalReport> list(Long userId) {
        return reportMapper.selectList(Wrappers.<MedicalReport>lambdaQuery()
                .eq(MedicalReport::getUserId, userId)
                .orderByDesc(MedicalReport::getCreateTime)
                .orderByDesc(MedicalReport::getId));
    }

    @Override
    public MedicalReport getDetail(Long id, Long userId) {
        MedicalReport report = reportMapper.selectById(id);
        if (report == null || !report.getUserId().equals(userId)) {
            throw new BusinessException("报告不存在或无权查看");
        }
        return report;
    }

    @Override
    public void delete(Long id, Long userId) {
        MedicalReport report = getDetail(id, userId);
        reportMapper.deleteById(id);
        // 磁盘文件删除失败不影响归档记录清理
        FileUtil.del(new File(BASE_PATH + report.getFilePath()));
    }
}
