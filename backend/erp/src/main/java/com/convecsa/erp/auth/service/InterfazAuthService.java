package com.convecsa.erp.auth.service;

import com.convecsa.erp.auth.dto.AuthResponse;
import com.convecsa.erp.auth.dto.LoginRequest;

public interface InterfazAuthService {

    AuthResponse login(LoginRequest request);
}
