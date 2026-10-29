package com.fitness.fitness.util;

import com.alibaba.excel.EasyExcel;
import com.fitness.fitness.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Excel 导出工具：统一处理月份解析、响应头和写文件
 */
public final class ExcelExportUtil {

    private ExcelExportUtil() {
    }

    /**
     * 解析 yyyy-MM 格式的月份，为空时取当前月
     */
    public static YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month.trim());
        } catch (DateTimeParseException e) {
            throw new BusinessException("月份格式应为 yyyy-MM");
        }
    }

    /**
     * 以 Excel 附件形式写入响应
     */
    public static void write(HttpServletResponse response, String fileName, String sheetName,
                             List<List<String>> head, List<List<Object>> rows) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encoded);
        EasyExcel.write(response.getOutputStream()).head(head).sheet(sheetName).doWrite(rows);
    }

    /**
     * null 转空字符串，避免导出出现 "null"
     */
    public static String text(String value) {
        return value == null ? "" : value;
    }

    /**
     * 空值转空字符串
     */
    public static Object value(Object value) {
        return value == null ? "" : value;
    }
}
