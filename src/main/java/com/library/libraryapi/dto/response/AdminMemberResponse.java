package com.library.libraryapi.dto.response;

import com.library.libraryapi.domain.entity.Member;
import com.library.libraryapi.domain.enums.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminMemberResponse {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private MemberRole role;
    private LocalDateTime createdAt;
    private long activeLoans;

    public static AdminMemberResponse from(Member member, long activeLoans) {
        return new AdminMemberResponse(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getEmail(),
                member.getRole(),
                member.getCreatedAt(),
                activeLoans
        );
    }
}
