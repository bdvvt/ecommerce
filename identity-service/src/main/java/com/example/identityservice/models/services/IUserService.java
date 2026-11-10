package com.example.identityservice.models.services;

import com.example.identityservice.models.constants.RoleName;
import com.example.identityservice.models.constants.UserStatus;
import com.example.identityservice.models.dto.req.*;
import com.example.identityservice.models.dto.res.PageResponse;
import com.example.identityservice.models.dto.res.UserRes;
import com.example.identityservice.models.entities.User;
import org.springframework.data.domain.Pageable;

public interface IUserService {
    UserRes createUser(User currentUser, UserReq req);
    UserRes updateUser(User currentUser, Long id, UserReq req);
    UserRes updateUserPassword(User currentUser, Long id, UserPassReq req);
    UserRes updateUserRole(User currentUser, Long id, UserRoleReq req);
    UserRes updateUserStatus(User currentUser, Long id, UserStatusReq req);
    UserRes findById(Long id);
    void deleteUser(Long id);
    PageResponse<UserRes> findAll(Pageable pageable, RoleName role, UserStatus status);
    PageResponse<UserRes> findAllBySearch(String search, Pageable pageable);

    // Profile & Password convenience
    UserRes getProfile(String identifier);
    UserRes updateProfile(String identifier, UpdateProfileReq req);
    void changePassword(String identifier, ChangePasswordReq req);
    UserRes findUserById(Long id);
}

