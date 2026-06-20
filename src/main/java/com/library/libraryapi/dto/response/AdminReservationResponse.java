package com.library.libraryapi.dto.response;

import com.library.libraryapi.domain.entity.Reservation;
import com.library.libraryapi.domain.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminReservationResponse {
    private Long id;
    private String username;
    private String bookTitle;
    private LocalDateTime reservedAt;
    private ReservationStatus status;

    public static AdminReservationResponse from(Reservation reservation) {
        return new AdminReservationResponse(
                reservation.getId(),
                reservation.getMember().getUsername(),
                reservation.getBook().getTitle(),
                reservation.getReservedAt(),
                reservation.getStatus()
        );
    }
}
