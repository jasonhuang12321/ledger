package com.learning;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ExcelExportService {

    public byte[] ownBills(List<LedgerEntry> rows) {
        return write(false, rows, Map.of());
    }

    public byte[] allBills(List<LedgerEntry> rows, Map<Long, String> usernames) {
        return write(true, rows, usernames);
    }

    public ResponseEntity<byte[]> download(byte[] body, String filename) {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(body);
    }

    private byte[] write(boolean withUser, List<LedgerEntry> rows, Map<Long, String> usernames) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("账单");
            Row header = sheet.createRow(0);
            int col = 0;
            if (withUser) {
                header.createCell(col++).setCellValue("用户");
            }
            header.createCell(col++).setCellValue("日期");
            header.createCell(col++).setCellValue("类型");
            header.createCell(col++).setCellValue("分类");
            header.createCell(col++).setCellValue("金额");
            header.createCell(col).setCellValue("备注");
            int r = 1;
            for (LedgerEntry entry : rows) {
                Row row = sheet.createRow(r++);
                int c = 0;
                if (withUser) {
                    String name = usernames.getOrDefault(entry.getUserId(), String.valueOf(entry.getUserId()));
                    row.createCell(c++).setCellValue(name);
                }
                row.createCell(c++).setCellValue(entry.getCreatedAt() == null ? "" : entry.getCreatedAt().toString());
                row.createCell(c++).setCellValue(entry.getType());
                row.createCell(c++).setCellValue(entry.getCategory());
                row.createCell(c++).setCellValue(entry.getAmount());
                row.createCell(c).setCellValue(entry.getNote() == null ? "" : entry.getNote());
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new BusinessException("导出失败");
        }
    }
}
