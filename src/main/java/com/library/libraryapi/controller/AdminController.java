package com.library.libraryapi.controller;

import com.library.libraryapi.domain.enums.LoanStatus;
import com.library.libraryapi.dto.response.AdminLoanResponse;
import com.library.libraryapi.dto.response.AdminMemberDetailResponse;
import com.library.libraryapi.dto.response.AdminMemberResponse;
import com.library.libraryapi.dto.response.AdminReservationResponse;
import com.library.libraryapi.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin", description = "관리자 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "전체 회원 목록 조회")
    @GetMapping("/members")
    public ResponseEntity<Page<AdminMemberResponse>> getAllMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getAllMembers(PageRequest.of(page, size)));
    }

    @Operation(summary = "회원 상세 조회 (대출 이력 포함)")
    @GetMapping("/members/{id}")
    public ResponseEntity<AdminMemberDetailResponse> getMemberDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getMemberDetail(id));
    }

    @Operation(summary = "전체 대출 현황 조회", description = "status 파라미터: ACTIVE, OVERDUE, RETURNED")
    @GetMapping("/loans")
    public ResponseEntity<Page<AdminLoanResponse>> getAllLoans(
            @RequestParam(required = false) LoanStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getAllLoans(status, PageRequest.of(page, size)));
    }

    @Operation(summary = "연체 대출 목록 조회 (자동 상태 업데이트)")
    @GetMapping("/loans/overdue")
    public ResponseEntity<List<AdminLoanResponse>> getOverdueLoans() {
        return ResponseEntity.ok(adminService.getOverdueLoans());
    }

    @Operation(summary = "전체 예약 현황 조회")
    @GetMapping("/reservations")
    public ResponseEntity<Page<AdminReservationResponse>> getAllReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getAllReservations(PageRequest.of(page, size)));
    }
}
