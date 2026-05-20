package com.walletapp.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1) // Ensures this runs first before security filters
public class TraceFilter implements Filter {

    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String CORRELATION_HEADER_NAME = "X-Correlation-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1. Check if upstream client or gateway already sent a correlation ID, otherwise generate one
        String correlationId = httpRequest.getHeader(CORRELATION_HEADER_NAME);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString().replace("-", "");
        }

        // 2. Put the ID into MDC (Logback context)
        MDC.put(CORRELATION_ID_KEY, correlationId);

        // 3. Optional: Send it back in the response header so clients can quote it for support
        httpResponse.setHeader(CORRELATION_HEADER_NAME, correlationId);

        try {
            // 4. Continue execution down the filter chain
            chain.doFilter(request, response);
        } finally {
            // 5. CRITICAL: Clear MDC when the request finishes to prevent memory leaks
            // and log pollution on reused Tomcat threads.
            MDC.remove(CORRELATION_ID_KEY);
        }
    }
}