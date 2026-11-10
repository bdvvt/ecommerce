package com.example.identityservice.models.services;

import com.example.identityservice.models.dto.res.PermissionRes;

import java.util.List;

/**
 * Service quản lý Permission theo chuẩn pm-xuatban-backend.
 */
public interface IPermissionService {

    List<PermissionRes> getPermissionsByFeatureName(String featureName);
}
