package com.walletapp.service.impl;

import com.walletapp.entity.AuditAction;
import com.walletapp.entity.AuditLog;
import com.walletapp.repository.AuditLogRepository;
import com.walletapp.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void log(String userName, AuditAction action, String description, String ipAddress) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setUserName(userName);
        auditLog.setDescription(description);
        auditLog.setIpAddress(ipAddress);
        auditLogRepository.save(auditLog);
    }
}
