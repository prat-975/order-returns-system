package com.orderrreturns.service;

import com.orderrreturns.dto.ReturnRequestDto;
import com.orderrreturns.entity.ItemCondition;
import com.orderrreturns.entity.ReturnRequest;
import com.orderrreturns.entity.ReturnStatus;
import com.orderrreturns.exception.AlreadyReviewedException;
import com.orderrreturns.repository.ReturnRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReturnServiceTest {

    @Mock
    private ReturnRequestRepository returnRequestRepository;

    @InjectMocks
    private ReturnService returnService;

    private ReturnRequestDto dto;

    @BeforeEach
    void setUp() {
        dto = new ReturnRequestDto();
        dto.setOrderId("ORD-TEST");
        dto.setProductName("Test Product");
        dto.setReturnReason("Testing");
    }

    @Test
    void submitReturnRequest_shouldSetPendingStatus() {
        dto.setPurchaseDate(LocalDate.now().minusDays(10));
        dto.setItemCondition(ItemCondition.NEW);

        when(returnRequestRepository.save(any(ReturnRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReturnRequest result = returnService.submitReturnRequest(dto, "testuser");

        assertEquals(ReturnStatus.PENDING, result.getStatus());
        assertEquals("Awaiting admin review", result.getRemarks());
        assertEquals("testuser", result.getSubmittedBy());
    }

    @Test
    void approveReturn_withinWindowAndNewCondition_shouldApprove() {
        ReturnRequest request = pendingRequest(LocalDate.now().minusDays(10), ItemCondition.NEW);
        when(returnRequestRepository.findById("req-1")).thenReturn(Optional.of(request));
        when(returnRequestRepository.save(any(ReturnRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReturnRequest result = returnService.approveReturn("req-1");

        assertEquals(ReturnStatus.APPROVED, result.getStatus());
        assertEquals("Your return has been approved", result.getRemarks());
    }

    @Test
    void approveReturn_withinWindowAndGoodCondition_shouldApprove() {
        ReturnRequest request = pendingRequest(LocalDate.now().minusDays(15), ItemCondition.GOOD);
        when(returnRequestRepository.findById("req-1")).thenReturn(Optional.of(request));
        when(returnRequestRepository.save(any(ReturnRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReturnRequest result = returnService.approveReturn("req-1");

        assertEquals(ReturnStatus.APPROVED, result.getStatus());
        assertEquals("Your return has been approved", result.getRemarks());
    }

    @Test
    void approveReturn_withinWindowAndDamagedCondition_shouldReject() {
        ReturnRequest request = pendingRequest(LocalDate.now().minusDays(5), ItemCondition.DAMAGED);
        when(returnRequestRepository.findById("req-1")).thenReturn(Optional.of(request));
        when(returnRequestRepository.save(any(ReturnRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReturnRequest result = returnService.approveReturn("req-1");

        assertEquals(ReturnStatus.REJECTED, result.getStatus());
        assertEquals("Item condition is DAMAGED — not eligible for return", result.getRemarks());
    }

    @Test
    void approveReturn_purchaseDateOlderThan30Days_shouldReject() {
        ReturnRequest request = pendingRequest(LocalDate.now().minusDays(31), ItemCondition.NEW);
        when(returnRequestRepository.findById("req-1")).thenReturn(Optional.of(request));
        when(returnRequestRepository.save(any(ReturnRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReturnRequest result = returnService.approveReturn("req-1");

        assertEquals(ReturnStatus.REJECTED, result.getStatus());
        assertEquals("Return window exceeded 30 days — not eligible for return", result.getRemarks());
    }

    @Test
    void rejectReturn_shouldSetRejectedByAdmin() {
        ReturnRequest request = pendingRequest(LocalDate.now().minusDays(5), ItemCondition.NEW);
        when(returnRequestRepository.findById("req-1")).thenReturn(Optional.of(request));
        when(returnRequestRepository.save(any(ReturnRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReturnRequest result = returnService.rejectReturn("req-1");

        assertEquals(ReturnStatus.REJECTED, result.getStatus());
        assertEquals("Declined by admin review", result.getRemarks());
    }

    @Test
    void rejectReturn_expiredWindow_shouldSetWindowExpiredRemark() {
        ReturnRequest request = pendingRequest(LocalDate.now().minusDays(40), ItemCondition.NEW);
        when(returnRequestRepository.findById("req-1")).thenReturn(Optional.of(request));
        when(returnRequestRepository.save(any(ReturnRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReturnRequest result = returnService.rejectReturn("req-1");

        assertEquals(ReturnStatus.REJECTED, result.getStatus());
        assertEquals("Return window exceeded 30 days — not eligible for return", result.getRemarks());
    }

    @Test
    void evaluateReturnEligibility_expiredWindowTakesPrecedenceOverDamagedCondition() {
        ReturnRequest request = new ReturnRequest();
        request.setPurchaseDate(LocalDate.now().minusDays(40));
        request.setItemCondition(ItemCondition.DAMAGED);

        returnService.evaluateReturnEligibility(request);

        assertEquals(ReturnStatus.REJECTED, request.getStatus());
        assertEquals("Return window exceeded 30 days — not eligible for return", request.getRemarks());
    }

    @Test
    void approveReturn_alreadyReviewed_shouldThrow() {
        ReturnRequest request = pendingRequest(LocalDate.now().minusDays(5), ItemCondition.NEW);
        request.setStatus(ReturnStatus.APPROVED);
        when(returnRequestRepository.findById("req-1")).thenReturn(Optional.of(request));

        assertThrows(AlreadyReviewedException.class, () -> returnService.approveReturn("req-1"));
    }

    @Test
    void submitReturnRequest_shouldPersistRequestViaRepository() {
        dto.setPurchaseDate(LocalDate.now().minusDays(3));
        dto.setItemCondition(ItemCondition.NEW);

        when(returnRequestRepository.save(any(ReturnRequest.class)))
                .thenAnswer(invocation -> {
                    ReturnRequest saved = invocation.getArgument(0);
                    saved.setId("test-id-1");
                    return saved;
                });

        returnService.submitReturnRequest(dto, "testuser");

        ArgumentCaptor<ReturnRequest> captor = ArgumentCaptor.forClass(ReturnRequest.class);
        verify(returnRequestRepository).save(captor.capture());

        ReturnRequest captured = captor.getValue();
        assertEquals("ORD-TEST", captured.getOrderId());
        assertEquals("testuser", captured.getSubmittedBy());
        assertEquals(ReturnStatus.PENDING, captured.getStatus());
    }

    @Test
    void getEligibilityInsight_windowExpired_shouldFlagIssue() {
        ReturnRequest request = pendingRequest(LocalDate.now().minusDays(45), ItemCondition.NEW);

        var insight = returnService.getEligibilityInsight(request);

        assertTrue(insight.isWindowExpired());
        assertEquals(45, insight.getDaysSincePurchase());
        assertEquals(15, insight.getDaysOverWindow());
        assertTrue(insight.hasEligibilityIssues());
    }

    @Test
    void getEligibilityInsight_damagedWithinWindow_shouldFlagDamagedOnly() {
        ReturnRequest request = pendingRequest(LocalDate.now().minusDays(10), ItemCondition.DAMAGED);

        var insight = returnService.getEligibilityInsight(request);

        assertFalse(insight.isWindowExpired());
        assertTrue(insight.isDamagedItem());
        assertTrue(insight.hasEligibilityIssues());
    }

    @Test
    void getEligibilityInsight_withinWindowAndGoodCondition_shouldBeEligible() {
        ReturnRequest request = pendingRequest(LocalDate.now().minusDays(12), ItemCondition.GOOD);

        var insight = returnService.getEligibilityInsight(request);

        assertTrue(insight.isWithinWindow());
        assertFalse(insight.hasEligibilityIssues());
        assertEquals(12, insight.getDaysSincePurchase());
    }

    @Test
    void resolveStatusMessage_legacyWindowExpiredRemark_shouldReturnUserFriendlyMessage() {
        ReturnRequest request = pendingRequest(LocalDate.now().minusDays(40), ItemCondition.NEW);
        request.setStatus(ReturnStatus.REJECTED);
        request.setRemarks("Return window expired");

        assertEquals("Return window exceeded 30 days — not eligible for return",
                returnService.resolveStatusMessage(request));
    }

    @Test
    void resolveStatusMessage_adminRejectRemarkWithExpiredWindow_shouldPreferWindowMessage() {
        ReturnRequest request = pendingRequest(LocalDate.now().minusDays(40), ItemCondition.NEW);
        request.setStatus(ReturnStatus.REJECTED);
        request.setRemarks("Declined by admin review");

        assertEquals("Return window exceeded 30 days — not eligible for return",
                returnService.resolveStatusMessage(request));
    }

    @Test
    void resolveStatusMessage_rejectedWithoutRemarks_shouldInferFromEligibility() {
        ReturnRequest request = pendingRequest(LocalDate.now().minusDays(40), ItemCondition.NEW);
        request.setStatus(ReturnStatus.REJECTED);
        request.setRemarks(null);

        assertEquals("Return window exceeded 30 days — not eligible for return",
                returnService.resolveStatusMessage(request));
    }

    private ReturnRequest pendingRequest(LocalDate purchaseDate, ItemCondition condition) {
        ReturnRequest request = new ReturnRequest();
        request.setId("req-1");
        request.setOrderId("ORD-TEST");
        request.setProductName("Test Product");
        request.setPurchaseDate(purchaseDate);
        request.setItemCondition(condition);
        request.setStatus(ReturnStatus.PENDING);
        request.setRemarks("Awaiting admin review");
        return request;
    }
}
