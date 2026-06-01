package com.walletapp.service;

import com.walletapp.entity.AuditAction;

public interface AuditLogService {

    void log(String userName, AuditAction action, String description, String ipAddress);
}
