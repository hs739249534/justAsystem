package com.neuqsoft.justAsystem.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuqsoft.justAsystem.model.RespBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication不支持" + request.getMethod());
        }

        String verify_code = (String) request.getSession().getAttribute("verify_code");
        if (request.getContentType().equals(MediaType.APPLICATION_JSON_UTF8_VALUE) || request.getContentType().equals(MediaType.APPLICATION_JSON_VALUE)) {
            Map<String, String> loginData = new HashMap<>();
            try {
                loginData = new ObjectMapper().readValue(request.getInputStream(), Map.class);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                String code = loginData.get("code");
                checkCode(code, verify_code);
            }
            String username = loginData.get(getUsernameParameter());
            String password = loginData.get(getPasswordParameter());
            if (username == null) {
                username = "";
            }

            if (password == null) {
                password = "";
            }

            username = username.trim();
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
            this.setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        } else {
            checkCode(request.getParameter("code"), verify_code);
            return super.attemptAuthentication(request, response);
        }
    }

    public void checkCode(String code, String verify_code) {
        if (code==null||verify_code==null||"".equals(code)||!verify_code.toLowerCase().equals(code.toLowerCase())) {
            throw new AuthenticationServiceException("验证码输入错误");
        }
    }
}