package com.onik.flowticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private String role;
    private Long status;
}
