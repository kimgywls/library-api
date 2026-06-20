package com.library.libraryapi.dto.response;

import com.library.libraryapi.domain.entity.Loan;
import com.library.libraryapi.domain.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LoanResponse {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private LocalDateTime loanDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private LoanStatus status;
    private boolean overdue;

    public static LoanResponse from(Loan loan) {
        boolean overdue = loan.getStatus() == LoanStatus.ACTIVE
                && LocalDateTime.now().isAfter(loan.getDueDate());
        return new LoanResponse(
                loan.getId(),
                loan.getBook().getId(),
                loan.getBook().getTitle(),
                loan.getBook().getAuthor(),
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                loan.getStatus(),
                overdue
        );
    }
}
