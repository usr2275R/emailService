package com.example.emailservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER = "API-KEY";
    public static final String CLIENT = "clientId";

    private final AppProperties properties;

    public ApiKeyFilter(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        return !request.getRequestURI().startsWith("/api");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws ServletException, IOException{
        String provided = request.getHeader(HEADER);
        String clientId = provided == null ? null: resolveClient(provided);

        if(clientId==null){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Missing or Invalid Api key\"}" );
            return;
        }
        request.setAttribute(CLIENT,clientId);
        chain.doFilter(request,response);
    }

    private String resolveClient(String provided){
        byte[] providedBytes = provided.getBytes(StandardCharsets.UTF_8);
        for(Map.Entry<String,String> e : properties.getSecurity().getApiKeys().entrySet()){
            if(MessageDigest.isEqual(providedBytes, e.getValue().getBytes(StandardCharsets.UTF_8))){
                return e.getKey();
            }
        }
        return null;
    }
}
