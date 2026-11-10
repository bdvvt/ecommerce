package com.example.identityservice.models.services.impl;

import lombok.RequiredArgsConstructor;
import com.example.identityservice.exceptions.BadRequestException;
import com.example.identityservice.exceptions.NotFoundException;
import com.example.identityservice.models.constants.RoleName;
import com.example.identityservice.models.constants.UserStatus;
import com.example.identityservice.models.dto.mapper.UserMapper;
import com.example.identityservice.models.dto.req.*;
import com.example.identityservice.models.dto.res.PageResponse;
import com.example.identityservice.models.dto.res.UserRes;
import com.example.identityservice.models.entities.Role;
import com.example.identityservice.models.entities.User;
import com.example.identityservice.models.repositories.RoleRepository;
import com.example.identityservice.models.repositories.UserRepository;
import com.example.identityservice.models.services.IUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    private User findUserByIdentifier(String identifier) {
        return userRepository.findByUsername(identifier)
                .or(() -> {
                    try {
                        Long id = Long.parseLong(identifier);
                        return userRepository.findById(id);
                    } catch (NumberFormatException e) {
                        return Optional.empty();
                    }
                })
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thông tin người dùng: " + identifier));
    }

    @Override
    @Transactional
    public UserRes createUser(User currentUser, UserReq req) {
        String identifier = req.getLoginIdentifier() != null ? req.getLoginIdentifier() : req.getUsername();
        if (identifier == null || identifier.isBlank()) {
            identifier = req.getEmail();
        }
        if (identifier == null || identifier.isBlank()) {
            throw new BadRequestException("Tên đăng nhập / định danh không được để trống");
        }
        if (userRepository.existsByLoginIdentifier(identifier)) {
            throw new BadRequestException("Tên đăng nhập hoặc email đã được sử dụng!");
        }

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getRoleName() == RoleName.ROLE_ADMIN);
        if (!isAdmin) {
            throw new BadRequestException("Bạn không có quyền tạo người dùng");
        }

        User user = userMapper.toEntity(req);
        user.setLoginIdentifier(identifier);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setStatus(UserStatus.ACTIVE);

        if (req.getRoleIds() != null && !req.getRoleIds().isEmpty()) {
            user.setRoles(new HashSet<>(roleRepository.findAllById(req.getRoleIds())));
        } else {
            Role userRole = roleRepository.findByRoleName(RoleName.ROLE_USER)
                    .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ROLE_USER, RoleName.ROLE_USER.name())));
            user.setRoles(Set.of(userRole));
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserRes findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng với ID: " + id));
        return userMapper.toResponse(user);
    }

    @Override
    public UserRes findUserById(Long id) {
        return findById(id);
    }

    @Override
    @Transactional
    public UserRes updateUser(User currentUser, Long id, UserReq req) {
        User updateUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng với ID: " + id));

        String identifier = req.getLoginIdentifier() != null ? req.getLoginIdentifier() : req.getUsername();
        if (identifier != null && !identifier.isBlank()) {
            userRepository.findByLoginIdentifier(identifier).ifPresent(existingUser -> {
                if (!existingUser.getUserId().equals(id)) {
                    throw new BadRequestException("Tên đăng nhập / định danh đã được sử dụng bởi tài khoản khác!");
                }
            });
            updateUser.setLoginIdentifier(identifier);
        }

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ROLE_ADMIN);
        boolean isOwner = updateUser.getUserId().equals(currentUser.getUserId());
        boolean targetIsAdmin = updateUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ROLE_ADMIN);

        if (targetIsAdmin && !isAdmin) {
            throw new BadRequestException("Không thể cập nhật thông tin của người dùng có quyền ADMIN!");
        }
        if (!isAdmin && !isOwner) {
            throw new BadRequestException("Bạn không có quyền chỉnh sửa thông tin người dùng này");
        }

        userMapper.updateEntity(req, updateUser);
        if (req.getRoleIds() != null && !req.getRoleIds().isEmpty() && isAdmin) {
            updateUser.setRoles(new HashSet<>(roleRepository.findAllById(req.getRoleIds())));
        }
        updateUser.setUpdatedAt(LocalDateTime.now());
        return userMapper.toResponse(userRepository.save(updateUser));
    }

    @Override
    @Transactional
    public UserRes updateUserPassword(User currentUser, Long id, UserPassReq req) {
        User updateUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng với ID: " + id));
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ROLE_ADMIN);
        boolean isOwner = updateUser.getUserId().equals(currentUser.getUserId());
        if (!isAdmin && !isOwner) {
            throw new BadRequestException("Bạn không có quyền chỉnh sửa mật khẩu người dùng này");
        }
        updateUser.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        updateUser.setUpdatedAt(LocalDateTime.now());
        return userMapper.toResponse(userRepository.save(updateUser));
    }

    @Override
    @Transactional
    public UserRes updateUserRole(User currentUser, Long id, UserRoleReq req) {
        User updateUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng với ID: " + id));
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ROLE_ADMIN);
        if (!isAdmin) {
            throw new BadRequestException("Bạn không có quyền cập nhật role người dùng!");
        }
        if (req.getRoleIds() != null) {
            updateUser.setRoles(new HashSet<>(roleRepository.findAllById(req.getRoleIds())));
        }
        updateUser.setUpdatedAt(LocalDateTime.now());
        return userMapper.toResponse(userRepository.save(updateUser));
    }


    @Override
    @Transactional
    public UserRes updateUserStatus(User currentUser, Long id, UserStatusReq req) {
        User updateUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng với ID: " + id));
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ROLE_ADMIN);
        if (!isAdmin) {
            throw new BadRequestException("Bạn không có quyền cập nhật trạng thái người dùng!");
        }
        if (updateUser.getRoles().stream().anyMatch(role -> role.getRoleName() == RoleName.ROLE_ADMIN)) {
            throw new BadRequestException("Không thể cập nhật trạng thái của tài khoản ADMIN!");
        }
        updateUser.setStatus(req.getStatus());
        updateUser.setUpdatedAt(LocalDateTime.now());
        return userMapper.toResponse(userRepository.save(updateUser));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User deleteUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng với ID: " + id));
        if (deleteUser.getRoles().stream().anyMatch(role -> role.getRoleName() == RoleName.ROLE_ADMIN)) {
            throw new BadRequestException("Không thể xóa người dùng có quyền ADMIN!");
        }
        userRepository.delete(deleteUser);
    }

    @Override
    public PageResponse<UserRes> findAll(Pageable pageable, RoleName role, UserStatus status) {
        Page<User> page = userRepository.findAllByRoleAndStatus(role, status, pageable);
        return PageResponse.of(page, userMapper.toResponseList(page.getContent()));
    }

    @Override
    public PageResponse<UserRes> findAllBySearch(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return findAll(pageable, null, null);
        }
        Page<User> page = userRepository.findAllBySearch(search, pageable);
        return PageResponse.of(page, userMapper.toResponseList(page.getContent()));
    }

    @Override
    public UserRes getProfile(String identifier) {
        User user = findUserByIdentifier(identifier);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserRes updateProfile(String identifier, UpdateProfileReq req) {
        User user = findUserByIdentifier(identifier);
        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            user.setFullName(req.getFullName());
        }
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            user.setPhone(req.getPhone());
        }
        user.setUpdatedAt(LocalDateTime.now());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(String identifier, ChangePasswordReq req) {
        User user = findUserByIdentifier(identifier);
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Mật khẩu cũ không chính xác");
        }
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }
}
