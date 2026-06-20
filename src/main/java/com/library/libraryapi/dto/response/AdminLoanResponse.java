package com.library.libraryapi.dto.response;

import com.library.libraryapi.domain.entity.Loan;
import com.library.libraryapi.domain.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminLoanResponse {
    private Long id;
    private String username;
    private String bookTitle;
    private LocalDateTime loanDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private LoanStatus status;

    public static AdminLoanResponse from(Loan loan) {
        LoanStatus status = (loan.getStatus() == LoanStatus.ACTIVE && loan.getDueDate().isBefore(LocalDateTime.now()))
                ? LoanStatus.OVERDUE : loan.getStatus();
        return new AdminLoanResponse(
                loan.getId(),
                loan.getMember().getUsername(),
                loan.getBook().getTitle(),
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                status
        );
    }
}
