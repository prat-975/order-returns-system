package com.orderrreturns.controller;

import com.orderrreturns.dto.ReturnEligibilityInsight;
import com.orderrreturns.dto.ReturnRequestDto;
import com.orderrreturns.entity.ReturnRequest;
import com.orderrreturns.entity.ReturnStatus;
import com.orderrreturns.service.ReturnService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ReturnController {

    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('USER')")
    public String showForm(Model model) {
        model.addAttribute("returnRequest", new ReturnRequestDto());
        return "index";
    }

    @PostMapping("/returns")
    @PreAuthorize("hasRole('USER')")
    public String submitReturn(@Valid @ModelAttribute("returnRequest") ReturnRequestDto returnRequest,
                               BindingResult bindingResult,
                               Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "index";
        }

        ReturnRequest saved = returnService.submitReturnRequest(returnRequest, authentication.getName());
        return "redirect:/returns/" + saved.getId();
    }

    @GetMapping("/my-returns")
    @PreAuthorize("hasRole('USER')")
    public String viewMyReturns(Authentication authentication, Model model) {
        populateReturnsModel(authentication.getName(), false, model);
        return "returns";
    }

    @GetMapping("/returns")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewAllReturns(Authentication authentication, Model model) {
        populateReturnsModel(authentication.getName(), true, model);
        return "returns";
    }

    @GetMapping("/returns/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String viewReturnStatus(@PathVariable String id,
                                   Authentication authentication,
                                   Model model) {
        boolean isAdmin = isAdmin(authentication);
        ReturnRequest returnRequest = returnService.getReturnById(id, authentication.getName(), isAdmin);
        model.addAttribute("returnRequest", returnRequest);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isPending", returnRequest.getStatus() == ReturnStatus.PENDING);
        model.addAttribute("returnsListUrl", isAdmin ? "/returns" : "/my-returns");
        if (isAdmin) {
            model.addAttribute("eligibilityInsight", returnService.getEligibilityInsight(returnRequest));
        }
        return "status";
    }

    @PostMapping("/returns/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public String approveReturn(@PathVariable String id, RedirectAttributes redirectAttributes) {
        ReturnRequest updated = returnService.approveReturn(id);
        redirectAttributes.addFlashAttribute("reviewMessage",
                "Request processed: " + updated.getStatus().name());
        return "redirect:/returns/" + id;
    }

    @PostMapping("/returns/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public String rejectReturn(@PathVariable String id, RedirectAttributes redirectAttributes) {
        returnService.rejectReturn(id);
        redirectAttributes.addFlashAttribute("reviewMessage", "Request rejected by admin");
        return "redirect:/returns/" + id;
    }

    private void populateReturnsModel(String username, boolean isAdmin, Model model) {
        List<ReturnRequest> returns = returnService.getReturnsForUser(username, isAdmin);

        model.addAttribute("returns", returns);
        model.addAttribute("pendingCount", countByStatus(returns, ReturnStatus.PENDING));
        model.addAttribute("approvedCount", countByStatus(returns, ReturnStatus.APPROVED));
        model.addAttribute("rejectedCount", countByStatus(returns, ReturnStatus.REJECTED));
        model.addAttribute("isAdmin", isAdmin);
        if (isAdmin) {
            Map<String, ReturnEligibilityInsight> eligibilityInsights = returns.stream()
                    .collect(Collectors.toMap(ReturnRequest::getId, returnService::getEligibilityInsight));
            model.addAttribute("eligibilityInsights", eligibilityInsights);
        }
    }

    private long countByStatus(List<ReturnRequest> returns, ReturnStatus status) {
        return returns.stream().filter(r -> r.getStatus() == status).count();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
