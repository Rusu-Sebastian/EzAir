package com.proiect.config;

public final class ApiEndpoints {
    private ApiEndpoints() {} // Prevent instantiation

    // User endpoints
    public static final String LOGIN = "/users/login";
    public static final String LOGIN_URL = LOGIN; // Alias for compatibility
    public static final String CREATE_ACCOUNT = "/users/creareCont";
    public static final String GET_USER = "/users/%s";
    public static final String UPDATE_USER = "/users/%s";
    public static final String DELETE_USER = "/users/%s";
    public static final String USER_SETTINGS = "/users/%s/setari";
    public static final String USER_TICKETS = "/users/%s/bilete";

    // Flight endpoints
    public static final String GET_FLIGHTS = "/zboruri/populareLista";
    public static final String SEARCH_FLIGHTS = "/zboruri/cautare";
    public static final String CREATE_FLIGHT = "/zboruri/adaugareZbor";
    public static final String UPDATE_FLIGHT = "/zboruri/%s";
    public static final String DELETE_FLIGHT = "/zboruri/stergereZbor/%s";
    public static final String GET_CITIES = "/zboruri/orase";
    
    // Keys
    public static final String USER_ID_KEY = "userId";
    public static final String IS_ADMIN_KEY = "esteAdmin";
}
