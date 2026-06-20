package com.library.libraryapi.dto.response;

import com.library.libraryapi.domain.entity.Loan;
import com.library.libraryapi.domain.entity.Member;
import com.library.libraryapi.domain.enums.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class AdminMemberDetailResponse {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private MemberRole role;
    private LocalDateTime createdAt;
    private long activeLoans;
    private List<AdminLoanResponse> loanHistory;

    public static AdminMemberDetailResponse from(Member member, long activeLoans, List<Loan> loans) {
        return new AdminMemberDetailResponse(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getEmail(),
                member.getRole(),
                member.getCreatedAt(),
                activeLoans,
                loans.stream().map(AdminLoanResponse::from).toList()
        );
    }
}
