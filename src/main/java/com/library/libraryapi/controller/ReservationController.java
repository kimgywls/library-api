package com.library.libraryapi.controller;

import com.library.libraryapi.dto.response.ReservationResponse;
import com.library.libraryapi.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Reservation", description = "예약 관리 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "내 예약 목록 조회")
    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reservationService.getMyReservations(userDetails.getUsername()));
    }

    @Operation(summary = "도서 예약 신청", responses = {
            @ApiResponse(responseCode = "201", description = "예약 성공"),
            @ApiResponse(responseCode = "409", description = "예약 불가 (AVAILABLE 도서 / 이미 예약 / 대출 중)")
    })
    @PostMapping("/reservations/{bookId}")
    public ResponseEntity<ReservationResponse> createReservation(
            @PathVariable Long bookId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(bookId, userDetails.getUsername()));
    }

    @Operation(summary = "예약 취소", responses = {
            @ApiResponse(responseCode = "204", description = "취소 성공"),
            @ApiResponse(responseCode = "403", description = "본인 예약 아님"),
            @ApiResponse(responseCode = "409", description = "대기 중 예약만 취소 가능")
    })
    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        reservationService.cancelReservation(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

}
