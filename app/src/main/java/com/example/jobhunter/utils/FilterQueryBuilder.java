package com.example.jobhunter.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FilterQueryBuilder {

    public static String buildFilterQuery(Map<String, List<String>> filters) {
        if (filters == null || filters.isEmpty()) {
            return "";
        }

        List<String> filterParts = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
            String field = entry.getKey();
            List<String> values = entry.getValue();

            if (values != null && !values.isEmpty()) {
                String joined = String.join("', '", values);
                String clause = field + " in ['" + joined + "']";
                filterParts.add(clause);
            }
        }

        if (filterParts.isEmpty()) {
            return "";
        }

        String filterString = String.join(" and ", filterParts);
        try {
            return URLEncoder.encode(filterString, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // This should not happen with UTF-8, but handle it just in case
            e.printStackTrace();
            return "";
        }
    }
} 