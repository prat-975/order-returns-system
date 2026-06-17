package com.orderrreturns.dto;

public class ReturnEligibilityInsight {

    private final long daysSincePurchase;
    private final int returnWindowDays;
    private final boolean futurePurchaseDate;
    private final boolean windowExpired;
    private final boolean damagedItem;

    public ReturnEligibilityInsight(long daysSincePurchase,
                                    int returnWindowDays,
                                    boolean futurePurchaseDate,
                                    boolean windowExpired,
                                    boolean damagedItem) {
        this.daysSincePurchase = daysSincePurchase;
        this.returnWindowDays = returnWindowDays;
        this.futurePurchaseDate = futurePurchaseDate;
        this.windowExpired = windowExpired;
        this.damagedItem = damagedItem;
    }

    public long getDaysSincePurchase() {
        return daysSincePurchase;
    }

    public int getReturnWindowDays() {
        return returnWindowDays;
    }

    public boolean isFuturePurchaseDate() {
        return futurePurchaseDate;
    }

    public boolean isWindowExpired() {
        return windowExpired;
    }

    public boolean isDamagedItem() {
        return damagedItem;
    }

    public boolean hasEligibilityIssues() {
        return futurePurchaseDate || windowExpired || damagedItem;
    }

    public boolean isWithinWindow() {
        return !futurePurchaseDate && !windowExpired;
    }

    public long getDaysOverWindow() {
        if (!windowExpired) {
            return 0;
        }
        return daysSincePurchase - returnWindowDays;
    }
}
