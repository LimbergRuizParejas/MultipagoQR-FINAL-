package com.example.Proveedores.Catalogo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if ("POST".equalsIgnoreCase(request.getMethod()) && path.startsWith("/api/companies")) {
            String contentType = request.getContentType();
            log.info("[REQ] {} {} contentType={}", request.getMethod(), path, contentType);

            // Parámetros (form-data text / query)
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String name = paramNames.nextElement();
                String value = request.getParameter(name);
                // Evitar loguear binarios
                if (!"logo".equalsIgnoreCase(name) && !"file".equalsIgnoreCase(name) && !"image".equalsIgnoreCase(name)) {
                    log.info("[REQ] param {}={} ", name, value);
                }
            }

            // Partes multipart (archivos)
            if (request instanceof MultipartHttpServletRequest multipart) {
                for (Map.Entry<String, MultipartFile> e : multipart.getFileMap().entrySet()) {
                    MultipartFile f = e.getValue();
                    if (f != null) {
                        log.info("[REQ] file part name={} filename={} size={}B partContentType={}",
                                e.getKey(), f.getOriginalFilename(), f.getSize(), f.getContentType());
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

