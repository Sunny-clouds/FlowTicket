package com.onik.flowticket.controller;

import com.onik.flowticket.common.ErrorMessage;
import com.onik.flowticket.common.PageResult;
import com.onik.flowticket.common.Result;
import com.onik.flowticket.dto.UserDto;
import com.onik.flowticket.dto.UserLoginDto;
import com.onik.flowticket.entity.User;
import com.onik.flowticket.service.UserService;
import com.onik.flowticket.utils.SecurityUtils;
import com.onik.flowticket.vo.UserLoginVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户接口", description = "用户登录、注册和管理接口")
@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private SecurityUtils securityUtils;

    @Operation(summary = "用户登录", description = "用户名和密码校验成功后返回 JWT token")
    @PostMapping("/login")
    public Result<UserLoginVo> login(@RequestBody UserLoginDto userLoginDto) {
        UserLoginVo user = userService.getByUserName(userLoginDto);
        if (user == null) {
            throw new RuntimeException(ErrorMessage.USER_NOT_FOUND);
        }
        return Result.success(user);
    }

    @Operation(summary = "用户注册", description = "注册为普通用户")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody UserDto userDto) {
        userService.register(userDto);
        return Result.success();
    }

    @Operation(summary = "当前登录用户")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/me")
    public Result<User> me() {
        return Result.success(securityUtils.currentUser());
    }

    @Operation(summary = "分页查询用户列表")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/getAll")
    public Result<PageResult<User>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userService.page(pageNum, pageSize));
    }

    @Operation(summary = "根据ID查询用户")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new RuntimeException(ErrorMessage.USER_NOT_FOUND);
        }
        return Result.success(user);
    }

    @Operation(summary = "新增用户", description = "仅管理员可访问")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('admin')")
    @PostMapping("/save")
    public Result<Void> create(@RequestBody UserDto userDto) {
        userService.save(userDto);
        return Result.success();
    }

    @Operation(summary = "修改用户", description = "仅管理员可访问")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('admin')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UserDto userDto) {
        userDto.setId(id);
        userService.updateById(userDto);
        return Result.success();
    }

    @Operation(summary = "删除用户", description = "仅管理员可访问")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }
}
