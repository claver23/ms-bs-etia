package com.etlions.ms_bs_etia.auth.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * Interceptor de aislamiento por empresa (Company Isolation).
 * Verifica que las requests a /api/v1/** solo accedan a datos
 * de la empresa asociada al JWT del usuario autenticado.
 *
 * Si el request incluye un companyId (query param) distinto
 * al del JWT → responde 403 Forbidden.
 */
@Slf4j
@Component
public class CompanyIsolationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String path = request.getRequestURI();

        // Solo aplica a /api/v1/** (excepto auth y onboarding)
        if (!path.startsWith("/api/v1/") || path.startsWith("/api/v1/auth/") || path.startsWith("/api/v1/onboarding")) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getDetails() == null) {
            return true; // Spring Security se encargará de rechazar si no está autenticado
        }

        // Extraer companyId del JWT (guardado en details por JwtAuthFilter)
        String jwtCompanyId = null;
        if (auth.getDetails() instanceof Map<?, ?> details) {
            Object cid = details.get("companyId");
            if (cid != null && !cid.toString().isBlank()) {
                jwtCompanyId = cid.toString();
            }
        }

        // Si el usuario no tiene companyId asignado (nuevo registro sin onboarding), permitir
        if (jwtCompanyId == null || jwtCompanyId.isEmpty()) {
            return true;
        }

        // Verificar companyId en query params
        String requestCompanyId = request.getParameter("companyId");
        if (requestCompanyId != null && !requestCompanyId.isBlank()
                && !requestCompanyId.equals(jwtCompanyId)) {
            log.warn("🚫 Company isolation violation: JWT companyId={}, request companyId={}, path={}",
                    jwtCompanyId, requestCompanyId, path);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"isSuccess\":false,\"message\":\"No tienes acceso a los datos de otra empresa\"}");
            return false;
        }

        return true;
    }
}

