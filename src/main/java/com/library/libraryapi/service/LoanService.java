package com.library.libraryapi.service;

import com.library.libraryapi.domain.entity.Book;
import com.library.libraryapi.domain.entity.Loan;
import com.library.libraryapi.domain.entity.Member;
import com.library.libraryapi.domain.enums.BookStatus;
import com.library.libraryapi.domain.enums.LoanStatus;
import com.library.libraryapi.domain.enums.ReservationStatus;
import com.library.libraryapi.dto.response.LoanResponse;
import com.library.libraryapi.exception.CustomException;
import com.library.libraryapi.repository.BookRepository;
import com.library.libraryapi.repository.LoanRepository;
import com.library.libraryapi.repository.MemberRepository;
import com.library.libraryapi.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public List<LoanResponse> getMyLoans(String username) {
        Member member = getMember(username);
        return loanRepository.findByMember(member).stream()
                .map(LoanResponse::from)
                .toList();
    }

    @Transactional
    public LoanResponse loanBook(Long bookId, String username) {
        Member member = getMember(username);
        Book book = bookRepository.findByIdWithLock(bookId)
                .orElseThrow(() -> new CustomException("도서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (book.getStatus() == BookStatus.RESERVED) {
            if (!reservationRepository.existsByMemberAndBookAndStatus(member, book, ReservationStatus.COMPLETED)) {
                throw new CustomException("예약된 도서입니다. 예약자만 대출할 수 있습니다.", HttpStatus.CONFLICT);
            }
        } else if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new CustomException("대출 가능한 도서가 아닙니다.", HttpStatus.CONFLICT);
        }

        Loan loan = Loan.builder()
                .member(member)
                .book(book)
                .dueDate(LocalDateTime.now().plusDays(14))
                .build();

        book.changeStatus(BookStatus.LOANED);
        bookRepository.save(book);

        return LoanResponse.from(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponse returnBook(Long loanId, String username) {
        Member member = getMember(username);
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new CustomException("대출 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (!loan.getMember().getId().equals(member.getId())) {
            throw new CustomException("본인의 대출만 반납할 수 있습니다.", HttpStatus.FORBIDDEN);
        }
        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new CustomException("이미 반납된 도서입니다.", HttpStatus.CONFLICT);
        }

        loan.returnBook();

        Book book = loan.getBook();
        reservationRepository.findFirstByBookAndStatusOrderByReservedAtAsc(book, ReservationStatus.WAITING)
                .ifPresentOrElse(
                        reservation -> {
                            reservation.complete();
                            book.changeStatus(BookStatus.RESERVED);
                        },
                        () -> book.changeStatus(BookStatus.AVAILABLE)
                );

        return LoanResponse.from(loanRepository.save(loan));
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateOverdueLoans() {
        LocalDateTime now = LocalDateTime.now();
        loanRepository.findByStatus(LoanStatus.ACTIVE).stream()
                .filter(loan -> loan.getDueDate().isBefore(now))
                .forEach(loan -> {
                    loan.markOverdue();
                    log.info("연체 처리: loanId={}, bookTitle={}", loan.getId(), loan.getBook().getTitle());
                });
    }

    private Member getMember(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }
}
