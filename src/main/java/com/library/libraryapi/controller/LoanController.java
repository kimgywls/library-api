package com.library.libraryapi.controller;

import com.library.libraryapi.dto.response.LoanResponse;
import com.library.libraryapi.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Loan", description = "대출 관리 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @Operation(summary = "내 대출 목록 조회")
    @GetMapping("/loans")
    public ResponseEntity<List<LoanResponse>> getMyLoans(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(loanService.getMyLoans(userDetails.getUsername()));
    }

    @Operation(summary = "도서 대출 신청", responses = {
            @ApiResponse(responseCode = "201", description = "대출 성공"),
            @ApiResponse(responseCode = "409", description = "대출 불가 (이미 대출 중)")
    })
    @PostMapping("/loans/{bookId}")
    public ResponseEntity<LoanResponse> loanBook(@PathVariable Long bookId,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loanService.loanBook(bookId, userDetails.getUsername()));
    }

    @Operation(summary = "도서 반납", responses = {
            @ApiResponse(responseCode = "200", description = "반납 성공"),
            @ApiResponse(responseCode = "403", description = "본인 대출 아님"),
            @ApiResponse(responseCode = "409", description = "이미 반납된 도서")
    })
    @PutMapping("/loans/return/{id}")
    public ResponseEntity<LoanResponse> returnBook(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(loanService.returnBook(id, userDetails.getUsername()));
    }

}
