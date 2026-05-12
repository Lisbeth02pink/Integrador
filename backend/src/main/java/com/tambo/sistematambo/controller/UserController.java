package com.tambo.sistematambo.controller;

import com.tambo.sistematambo.dto.UserRequest;
import com.tambo.sistematambo.dto.UserUpdateRequest;
import com.tambo.sistematambo.response.UserResponse;
import com.tambo.sistematambo.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> listar() {
        return userService.listar();
    }

    @PostMapping
    public UserResponse crear(@Valid @RequestBody UserRequest request) {
        return userService.crear(request);
    }

    @PutMapping("/{userId}")
    public UserResponse actualizar(@PathVariable Long userId, @Valid @RequestBody UserUpdateRequest request) {
        return userService.actualizar(userId, request);
    }

    @PatchMapping("/{userId}/estado")
    public UserResponse cambiarEstado(@PathVariable Long userId) {
        return userService.cambiarEstado(userId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long userId) {
        userService.eliminar(userId);
    }
}
