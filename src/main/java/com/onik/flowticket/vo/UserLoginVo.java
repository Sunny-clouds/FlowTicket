package com.onik.flowticket.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginVo {
    private Long id;
    private String username;
    private String nickname;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private String role;
    private String token;
}
