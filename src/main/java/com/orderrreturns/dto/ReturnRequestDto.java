package com.orderrreturns.dto;

import com.orderrreturns.entity.ItemCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public class ReturnRequestDto {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Product Name is required")
    private String productName;

    @NotNull(message = "Purchase Date is required")
    @PastOrPresent(message = "Purchase Date cannot be in the future")
    private LocalDate purchaseDate;

    @NotBlank(message = "Return Reason is required")
    private String returnReason;

    @NotNull(message = "Item Condition is required")
    private ItemCondition itemCondition;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public ItemCondition getItemCondition() {
        return itemCondition;
    }

    public void setItemCondition(ItemCondition itemCondition) {
        this.itemCondition = itemCondition;
    }
}
