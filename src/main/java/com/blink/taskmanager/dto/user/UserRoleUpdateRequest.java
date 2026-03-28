package com.blink.taskmanager.dto.user;

import com.blink.taskmanager.model.Role;
import jakarta.validation.constraints.NotNull;

public class UserRoleUpdateRequest {

    @NotNull
    private Role role;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
