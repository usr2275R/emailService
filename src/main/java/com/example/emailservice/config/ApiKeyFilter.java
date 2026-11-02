package com.example.emailservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-API-KEY";
    public static final String CLIENT = "clientId";

  private final AppProperties properties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String path= request.getRequestURI();
        return path.startsWith("/swagger-ui")|| path.startsWith("/v3/api-docs");
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

        Authentication authentication = new UsernamePasswordAuthenticationToken(clientId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
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
