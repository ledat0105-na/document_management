package com.example.document_management.util;

import com.example.document_management.model.DocumentFile;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CsvUtils {
    private static final String SEP = ",";

    public static String toCsv(List<DocumentFile> docs) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,title,description,originalFilename,storedFilename,createdAt,updatedAt\n");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        for (DocumentFile d : docs) {
            sb.append(escape(d.getId()))
                    .append(SEP).append(escape(d.getTitle()))
                    .append(SEP).append(escape(d.getDescription()))
                    .append(SEP).append(escape(d.getOriginalFilename()))
                    .append(SEP).append(escape(d.getStoredFilename()))
                    .append(SEP).append(escape(fmt.format(d.getCreatedAt())))
                    .append(SEP).append(escape(fmt.format(d.getUpdatedAt())))
                    .append("\n");
        }
        return sb.toString();
    }

    private static String escape(Object o) {
        if (o == null) return "\"\"";
        String s = String.valueOf(o).replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}


