package com.backend.ms_security.service;

import org.springframework.stereotype.Service;

import com.backend.ms_security.entity.Role;
import com.backend.ms_security.entity.User;
import com.backend.ms_security.entity.UserRole;
import com.backend.ms_security.exception.ApplicationException;
import com.backend.ms_security.exception.ErrorCase;
import com.backend.ms_security.repository.RoleRepository;
import com.backend.ms_security.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GuestRoleService {
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public void assignGuestRole(User user) {
        if (user == null || user.getId() == null) {
            throw new ApplicationException(ErrorCase.INVALID_OPERATION, "The user must be persisted before assigning a role.");
        }

        Role guestRole = roleRepository.findByNameIgnoreCase("GUEST")
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "The GUEST role is not configured."
                ));

        if (userRoleRepository.existsByUserIdAndRoleId(user.getId(), guestRole.getId())) {
            return;
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(guestRole);
        userRoleRepository.save(userRole);
    }
}
