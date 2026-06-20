package com.library.libraryapi.dto.response;

import com.library.libraryapi.domain.entity.Reservation;
import com.library.libraryapi.domain.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private String bookTitle;
    private String bookAuthor;
    private LocalDateTime reservedAt;
    private ReservationStatus status;

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getBook().getTitle(),
                reservation.getBook().getAuthor(),
                reservation.getReservedAt(),
                reservation.getStatus()
        );
    }
}
