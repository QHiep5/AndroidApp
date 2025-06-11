package com.example.jobhunter.api;

public class ApiConfig {
    public static final String BASE_HOST_URL = "http://192.168.1.107:8080/";
    public static final String BASE_URL = BASE_HOST_URL + "api/v1/";
    public static final String LOGO_BASE_URL = BASE_HOST_URL + "storage/company/";

    // Endpoint cho từng entity
    public static final String USER = BASE_URL + "users";
    public static final String COMPANY = BASE_URL + "companies";
    public static final String JOB = BASE_URL + "jobs";
    public static final String RESUME = BASE_URL + "resumes";
    public static final String SKILL = BASE_URL + "skills";
    public static final String FILE = BASE_URL + "files";
    public static final String PERMISSION = BASE_URL + "permissions";
    public static final String ROLE = BASE_URL + "roles";
    public static final String SUBSCRIBER = BASE_URL + "subscribers";

    // Endpoint cho xác thực (nếu backend tách riêng /api/auth/)
    public static final String AUTH_LOGIN = BASE_URL + "auth/login";
    public static final String AUTH_REGISTER = BASE_URL.replace("/v1/", "/auth/register");
}
