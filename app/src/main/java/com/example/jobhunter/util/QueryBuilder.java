package com.example.jobhunter.util;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp tiện ích để xây dựng các chuỗi truy vấn (query) cho API.
 */
public class QueryBuilder {

    /**
     * Xây dựng một mệnh đề 'IN' cho truy vấn filter.
     * Ví dụ: buildInClause("skills", ["Java", "Kotlin"]) -> 'skills in ("Java","Kotlin")'
     *
     * @param field Tên của trường cần lọc (ví dụ: "skills", "location").
     * @param values Danh sách các giá trị để đưa vào mệnh đề IN.
     * @return Một chuỗi được định dạng cho mệnh đề IN, hoặc một chuỗi rỗng nếu danh sách giá trị rỗng.
     */
    public static String buildInClause(String field, List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }

        // Bọc mỗi giá trị trong dấu ngoặc kép
        List<String> quotedValues = new ArrayList<>();
        for (String value : values) {
            quotedValues.add(String.format("\"%s\"", value));
        }

        // Nối các giá trị bằng dấu phẩy
        String joinedValues = TextUtils.join(",", quotedValues);

        return String.format("%s in (%s)", field, joinedValues);
    }
} 