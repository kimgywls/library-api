package com.library.libraryapi.service;

import com.library.libraryapi.domain.entity.Book;
import com.library.libraryapi.domain.entity.Member;
import com.library.libraryapi.domain.entity.Reservation;
import com.library.libraryapi.domain.enums.BookStatus;
import com.library.libraryapi.domain.enums.LoanStatus;
import com.library.libraryapi.domain.enums.ReservationStatus;
import com.library.libraryapi.dto.response.ReservationResponse;
import com.library.libraryapi.exception.CustomException;
import com.library.libraryapi.repository.BookRepository;
import com.library.libraryapi.repository.LoanRepository;
import com.library.libraryapi.repository.MemberRepository;
import com.library.libraryapi.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;

    public List<ReservationResponse> getMyReservations(String username) {
        Member member = getMember(username);
        return reservationRepository.findByMember(member).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse createReservation(Long bookId, String username) {
        Member member = getMember(username);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException("도서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (book.getStatus() == BookStatus.AVAILABLE) {
            throw new CustomException("대출 가능한 도서는 예약할 수 없습니다. 바로 대출하세요.", HttpStatus.CONFLICT);
        }
        if (loanRepository.existsByMemberAndBookAndStatusIn(member, book,
                List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE))) {
            throw new CustomException("현재 대출 중인 도서는 예약할 수 없습니다.", HttpStatus.CONFLICT);
        }
        if (reservationRepository.existsByMemberAndBookAndStatus(member, book, ReservationStatus.WAITING)) {
            throw new CustomException("이미 예약한 도서입니다.", HttpStatus.CONFLICT);
        }

        Reservation reservation = Reservation.builder()
                .member(member)
                .book(book)
                .build();

        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional
    public void cancelReservation(Long id, String username) {
        Member member = getMember(username);
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new CustomException("예약 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (!reservation.getMember().getId().equals(member.getId())) {
            throw new CustomException("본인의 예약만 취소할 수 있습니다.", HttpStatus.FORBIDDEN);
        }
        if (reservation.getStatus() != ReservationStatus.WAITING) {
            throw new CustomException("대기 중인 예약만 취소할 수 있습니다.", HttpStatus.CONFLICT);
        }

        reservation.cancel();
    }

    private Member getMember(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }
}
