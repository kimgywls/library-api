package com.library.libraryapi.service;

import com.library.libraryapi.domain.entity.Loan;
import com.library.libraryapi.domain.entity.Member;
import com.library.libraryapi.domain.enums.LoanStatus;
import com.library.libraryapi.dto.response.AdminLoanResponse;
import com.library.libraryapi.dto.response.AdminMemberDetailResponse;
import com.library.libraryapi.dto.response.AdminMemberResponse;
import com.library.libraryapi.dto.response.AdminReservationResponse;
import com.library.libraryapi.exception.CustomException;
import com.library.libraryapi.repository.LoanRepository;
import com.library.libraryapi.repository.MemberRepository;
import com.library.libraryapi.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;

    public Page<AdminMemberResponse> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(member -> AdminMemberResponse.from(member,
                        loanRepository.countByMemberAndStatus(member, LoanStatus.ACTIVE)));
    }

    public AdminMemberDetailResponse getMemberDetail(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new CustomException("회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        long activeLoans = loanRepository.countByMemberAndStatus(member, LoanStatus.ACTIVE);
        List<Loan> loans = loanRepository.findByMember(member);
        return AdminMemberDetailResponse.from(member, activeLoans, loans);
    }

    public Page<AdminLoanResponse> getAllLoans(LoanStatus status, Pageable pageable) {
        if (status != null) {
            return loanRepository.findByStatus(status, pageable).map(AdminLoanResponse::from);
        }
        return loanRepository.findAll(pageable).map(AdminLoanResponse::from);
    }

    @Transactional
    public List<AdminLoanResponse> getOverdueLoans() {
        LocalDateTime now = LocalDateTime.now();
        List<Loan> overdueLoans = loanRepository.findByStatusAndDueDateBefore(LoanStatus.ACTIVE, now);
        overdueLoans.forEach(Loan::markOverdue);
        return overdueLoans.stream().map(AdminLoanResponse::from).toList();
    }

    public Page<AdminReservationResponse> getAllReservations(Pageable pageable) {
        return reservationRepository.findAll(pageable).map(AdminReservationResponse::from);
    }
}
