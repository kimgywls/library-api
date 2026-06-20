package com.library.libraryapi.repository;

import com.library.libraryapi.domain.entity.Book;
import com.library.libraryapi.domain.entity.Loan;
import com.library.libraryapi.domain.entity.Member;
import com.library.libraryapi.domain.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    boolean existsByMemberAndBookAndStatusIn(Member member, Book book, List<LoanStatus> statuses);
    long countByMemberAndStatus(Member member, LoanStatus status);
    List<Loan> findByMemberAndStatus(Member member, LoanStatus status);
    List<Loan> findByMember(Member member);
    List<Loan> findByStatus(LoanStatus status);
    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);
    List<Loan> findByStatusAndDueDateBefore(LoanStatus status, LocalDateTime dueDate);
}
