package com.library.libraryapi.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    private String nickname;
    private String email;
}
