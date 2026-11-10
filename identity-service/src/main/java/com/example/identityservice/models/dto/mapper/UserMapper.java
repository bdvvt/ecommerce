package com.example.identityservice.models.dto.mapper;

import com.example.identityservice.models.dto.req.UserReq;
import com.example.identityservice.models.dto.res.UserRes;
import com.example.identityservice.models.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "otpCode", ignore = true)
    @Mapping(target = "otpExpiration", ignore = true)
    @Mapping(target = "status", ignore = true)
    User toEntity(UserReq req);

    UserRes toResponse(User user);

    List<UserRes> toResponseList(List<User> users);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "loginIdentifier", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "otpCode", ignore = true)
    @Mapping(target = "otpExpiration", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntity(UserReq req, @MappingTarget User user);
}
