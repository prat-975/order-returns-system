package com.orderrreturns.service;

import com.orderrreturns.dto.ReturnEligibilityInsight;
import com.orderrreturns.dto.ReturnRequestDto;
import com.orderrreturns.entity.ItemCondition;
import com.orderrreturns.entity.ReturnRequest;
import com.orderrreturns.entity.ReturnStatus;
import com.orderrreturns.exception.AlreadyReviewedException;
import com.orderrreturns.exception.ResourceNotFoundException;
import com.orderrreturns.repository.ReturnRequestRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReturnService {

    private static final int RETURN_WINDOW_DAYS = 30;
    private static final String REMARK_PENDING = "Awaiting admin review";
    private static final String REMARK_APPROVED = "Return approved";
    private static final String REMARK_WINDOW_EXPIRED = "Return window expired";
    private static final String REMARK_DAMAGED = "Damaged item not eligible";
    private static final String REMARK_ADMIN_REJECTED = "Rejected by admin review";

    private final ReturnRequestRepository returnRequestRepository;

    public ReturnService(ReturnRequestRepository returnRequestRepository) {
        this.returnRequestRepository = returnRequestRepository;
    }

    public ReturnRequest submitReturnRequest(ReturnRequestDto dto, String username) {
        ReturnRequest request = new ReturnRequest();
        request.setOrderId(dto.getOrderId());
        request.setProductName(dto.getProductName());
        request.setPurchaseDate(dto.getPurchaseDate());
        request.setReturnReason(dto.getReturnReason());
        request.setItemCondition(dto.getItemCondition());
        request.setSubmittedBy(username);
        request.setStatus(ReturnStatus.PENDING);
        request.setRemarks(REMARK_PENDING);
        request.setCreatedAt(LocalDateTime.now());

        return returnRequestRepository.save(request);
    }

    public ReturnRequest approveReturn(String id) {
        ReturnRequest request = findPendingRequest(id);
        evaluateReturnEligibility(request);
        request.setReviewedAt(LocalDateTime.now());
        return returnRequestRepository.save(request);
    }

    public ReturnRequest rejectReturn(String id) {
        ReturnRequest request = findPendingRequest(id);
        request.setStatus(ReturnStatus.REJECTED);
        request.setRemarks(REMARK_ADMIN_REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        return returnRequestRepository.save(request);
    }

    void evaluateReturnEligibility(ReturnRequest request) {
        if (isReturnWindowExpired(request.getPurchaseDate())) {
            request.setStatus(ReturnStatus.REJECTED);
            request.setRemarks(REMARK_WINDOW_EXPIRED);
        } else if (request.getItemCondition() == ItemCondition.DAMAGED) {
            request.setStatus(ReturnStatus.REJECTED);
            request.setRemarks(REMARK_DAMAGED);
        } else {
            request.setStatus(ReturnStatus.APPROVED);
            request.setRemarks(REMARK_APPROVED);
        }
    }

    boolean isReturnWindowExpired(LocalDate purchaseDate) {
        long daysSincePurchase = ChronoUnit.DAYS.between(purchaseDate, LocalDate.now());
        return daysSincePurchase > RETURN_WINDOW_DAYS;
    }

    public ReturnEligibilityInsight getEligibilityInsight(ReturnRequest request) {
        long daysSincePurchase = ChronoUnit.DAYS.between(request.getPurchaseDate(), LocalDate.now());
        boolean futurePurchaseDate = daysSincePurchase < 0;
        boolean windowExpired = daysSincePurchase > RETURN_WINDOW_DAYS;
        boolean damagedItem = request.getItemCondition() == ItemCondition.DAMAGED;

        return new ReturnEligibilityInsight(
                daysSincePurchase,
                RETURN_WINDOW_DAYS,
                futurePurchaseDate,
                windowExpired,
                damagedItem
        );
    }

    public ReturnEligibilityInsight getEligibilityInsight(LocalDate purchaseDate, ItemCondition itemCondition) {
        long daysSincePurchase = ChronoUnit.DAYS.between(purchaseDate, LocalDate.now());
        boolean futurePurchaseDate = daysSincePurchase < 0;
        boolean windowExpired = daysSincePurchase > RETURN_WINDOW_DAYS;
        boolean damagedItem = itemCondition == ItemCondition.DAMAGED;

        return new ReturnEligibilityInsight(
                daysSincePurchase,
                RETURN_WINDOW_DAYS,
                futurePurchaseDate,
                windowExpired,
                damagedItem
        );
    }

    public int getReturnWindowDays() {
        return RETURN_WINDOW_DAYS;
    }

    public ReturnRequest getReturnById(String id, String username, boolean isAdmin) {
        ReturnRequest request = returnRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found with id: " + id));

        if (!isAdmin && !username.equals(request.getSubmittedBy())) {
            throw new AccessDeniedException("You are not allowed to view this return request");
        }

        return request;
    }

    public List<ReturnRequest> getReturnsForUser(String username, boolean isAdmin) {
        if (isAdmin) {
            return returnRequestRepository.findAllByOrderByCreatedAtDesc();
        }
        return returnRequestRepository.findBySubmittedByOrderByCreatedAtDesc(username);
    }

    private ReturnRequest findPendingRequest(String id) {
        ReturnRequest request = returnRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found with id: " + id));

        if (request.getStatus() != ReturnStatus.PENDING) {
            throw new AlreadyReviewedException("This return request has already been reviewed");
        }

        return request;
    }
}
