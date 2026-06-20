package com.library.libraryapi.repository;

import com.library.libraryapi.domain.entity.Book;
import com.library.libraryapi.domain.entity.Member;
import com.library.libraryapi.domain.entity.Reservation;
import com.library.libraryapi.domain.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByMember(Member member);
    List<Reservation> findByBookAndStatus(Book book, ReservationStatus status);
    Optional<Reservation> findFirstByBookAndStatusOrderByReservedAtAsc(Book book, ReservationStatus status);
    boolean existsByMemberAndBookAndStatus(Member member, Book book, ReservationStatus status);
}
