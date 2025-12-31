package pizza_shop_system.order.entities;

import pizza_shop_system.account.entities.User;

import java.time.LocalDate;

public class CreditCard {

    private String type;
    private String cardNumber;
    private String holderName;
    private LocalDate expDate;
    private int cvcNum;

    public CreditCard(){
        this.type = "DEFAULT TYPE";
        this.cardNumber = "0000000000000000";
        this.holderName = "DEFAULT NAME";
        this.expDate = LocalDate.now();
        this.cvcNum = 111;
    }

    public CreditCard(String type, String number, String holderName, LocalDate expDate, int cvcNum){
        this.type = type;
        this.cardNumber = number;
        this.holderName = holderName;
        this.expDate = expDate;
        this.cvcNum = cvcNum;
    }

    //Getters
    public String getType(){ return this.type; }
    public String getCardNumber(){ return  this.cardNumber; }
    public String getHolderName(){ return this.holderName; }
    public LocalDate getExpDate(){ return this.expDate; }
    public int getCvcNum(){ return this.cvcNum; }

    //Setters
    public void setType(String newType){ this.type = newType; }
    public void setCardNumber(String newNumber){ this.cardNumber = newNumber; }
    public void setHolderName(String newName){ this.holderName = newName; }
    public void setExpDate(LocalDate newDate){ this.expDate = newDate; }
    public void setCvcNum(int newNumber){ this.cvcNum = newNumber; }

    public boolean verifyCard(User account) {
        try {

            if (account == null || account.getCreditCard() == null) {
                System.err.println("Error: Credit Card is null. There is no card to verify!");
                return false;
            }

            CreditCard card = account.getCreditCard();
            String creditCardNumber = card.getCardNumber().replaceAll("\\D", "");

            //Verify Exp Date first
            LocalDate currentDate = LocalDate.now();
            int currentMonth = currentDate.getMonthValue();
            int currentYear = currentDate.getYear();

            int creditCardMonth = card.getExpDate().getMonthValue();
            int creditCardYear = card.getExpDate().getYear();

            if (creditCardYear < currentYear || (creditCardYear == currentYear && creditCardMonth < currentMonth)) {
                System.err.println("The card has expired.");
                return false;
            }

            // Validate Card Type and Number Length
            String cardType = card.getType().trim();    //Trimming the cardType for ease of use
            String cardCVC = String.valueOf(card.getCvcNum());

            if (creditCardNumber.length() == 15 && cardCVC.length() == 4 &&
                    (cardType.equalsIgnoreCase("AMEX") || cardType.equalsIgnoreCase("American Express"))) {
                return true;  // Valid AMEX card
            } else if (creditCardNumber.length() == 16 && cardCVC.length() == 4 &&
                    (cardType.equalsIgnoreCase("MasterCard") ||
                            cardType.equalsIgnoreCase("Discover") ||
                            cardType.equalsIgnoreCase("Visa"))) {
                return true;  // Valid MasterCard, Discover, or Visa card
            } else {
                System.err.println("Invalid card type or number length.");
                return false;  // Invalid card type or length
            }
        } catch (NumberFormatException e) {
            System.err.println("NumberFormatException: Card number contains invalid characters.");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error has occurred while verifying the card.");
            e.printStackTrace();
            return false;
        }

    }

}
