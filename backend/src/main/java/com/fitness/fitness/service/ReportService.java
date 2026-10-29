package com.fitness.fitness.service;

import com.fitness.fitness.entity.MedicalReport;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    /** 上传体检报告并归档 */
    MedicalReport upload(MultipartFile file, LocalDate reportDate, Long userId) throws IOException;

    /** 查询本人全部报告，按上传时间倒序 */
    List<MedicalReport> list(Long userId);

    /** 查询报告详情，仅限本人 */
    MedicalReport getDetail(Long id, Long userId);

    /** 删除报告及磁盘文件，仅限本人 */
    void delete(Long id, Long userId);
}
