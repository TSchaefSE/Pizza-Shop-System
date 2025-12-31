package pizza_shop_system.order.entities;

import pizza_shop_system.account.entities.User;
import pizza_shop_system.order.services.OrderService;

public class Payment {

    private double amount;
    private String paymentType;
    private boolean isProcessed;
    private final OrderService orderService;

    public Payment(OrderService orderService) {
        this.orderService = orderService;
        this.amount = 0;
        this.paymentType = "DEFAULT";
        this.isProcessed = false;
    }

    public Payment(OrderService orderService, double amount, String paymentType, boolean isProcessed) {
        this.orderService = orderService;
        this.amount = amount;
        this.paymentType = paymentType;
        this.isProcessed = isProcessed;
    }

    // Getters
    public double getAmount() { return this.amount; }
    public String getPaymentType() { return this.paymentType; }
    public boolean getIsProcessed() { return this.isProcessed; }

    // Setters
    public void setAmount(double newAmount) { this.amount = newAmount; }
    public void setPaymentType(String newPaymentType) { this.paymentType = newPaymentType; }
    public void setProcessed(boolean newProcessed) { this.isProcessed = newProcessed; }


    public void processCheck(double checkAmount) {
        try {
            if (checkAmount < 0) {
                System.err.println("Error: Check amount cannot be negative");
                setProcessed(false);
                return;
            }

            double total = orderService.getCurrentOrderTotal();
            //System.out.println("Processing check payment. Total: $" + total);

            setPaymentType("Check");
            this.amount = total;

            if (total > 0 && checkAmount >= total) {
                setProcessed(true);
                orderService.finalizeOrder();
                //System.out.println("Check payment processed successfully.");
            } else {
                setProcessed(false);
                System.err.println("Payment failed: insufficient check amount or zero total.");
            }

        } catch (Exception e) {
            System.err.println("Error during check payment processing.");
            e.printStackTrace();
            setProcessed(false);
        }
    }

    public void processCash(double cashAmount) {
        try {
            //System.out.println("Processing Cash payment. Amount: $" + cashAmount);
            if (cashAmount < 0) {
                System.err.println("Error: Cash amount cannot be negative");
                setProcessed(false);
                return;
            }

            double total = orderService.getCurrentOrderTotal();
            //System.out.println("Processing cash payment. Total: $" + total);

            setPaymentType("Cash");
            this.amount = total;

            if (total > 0 && cashAmount >= total) {
                double change = Math.round((cashAmount - total) * 100.0) / 100.0;
                setProcessed(true);
                orderService.finalizeOrder();
                //System.out.println("Cash payment processed successfully. Change: $" + change);
            } else {
                setProcessed(false);
                System.err.println("Payment failed: insufficient cash or zero total.");
            }

        } catch (Exception e) {
            System.err.println("Error during cash payment processing.");
            e.printStackTrace();
            setProcessed(false);
        }
    }

    public void processCard(User account) {
        try {
            /*
            if (account == null || account.getCreditCard() == null) {
                System.err.println("Error: Account or credit card is null");
                setProcessed(false);
                return;
            }

            if (!account.getCreditCard().verifyCard(account)) {
                System.err.println("Card verification failed.");
                setProcessed(false);
                return;
            }

             */

            double total = orderService.getCurrentOrderTotal();
            //System.out.println("Processing card payment. Total: $" + total);

            setPaymentType("Card");
            this.amount = total;

            if (total > 0) {
                setProcessed(true);
                orderService.finalizeOrder();
                //System.out.println("Card payment processed successfully.");
            } else {
                setProcessed(false);
                System.err.println("Payment failed: zero total.");
            }

        } catch (Exception e) {
            System.err.println("Error during card payment processing.");
            e.printStackTrace();
            setProcessed(false);
        }
    }
}
