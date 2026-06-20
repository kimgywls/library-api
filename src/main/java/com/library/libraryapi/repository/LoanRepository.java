package com.library.libraryapi.repository;

import com.library.libraryapi.domain.entity.Book;
import com.library.libraryapi.domain.entity.Loan;
import com.library.libraryapi.domain.entity.Member;
import com.library.libraryapi.domain.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    boolean existsByMemberAndBookAndStatusIn(Member member, Book book, List<LoanStatus> statuses);
    List<Loan> findByMemberAndStatus(Member member, LoanStatus status);
    List<Loan> findByMember(Member member);
    List<Loan> findByStatus(LoanStatus status);
}
