package pizza_shop_system.account.entities;

import pizza_shop_system.order.entities.CreditCard;

public class User {
    private String name, email, address, phoneNumber, password, accountType;
    private int id;
    private CreditCard creditCard;

    public User(int id, String accountType, String email, String password, String name, String address, String phoneNumber){
        this.name = name;
        this.id = id;
        this.email = email;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.accountType = accountType;
    }

    public User(String customerName, int customerID) {
    }

    //Getters
    public String getName(){ return this.name;}
    public int getId(){ return this.id; }
    public String getEmail(){ return this.email;}
    public String getAddress(){ return this.address;}
    public String getPhoneNumber(){ return this.phoneNumber;}
    public String getPassword(){ return this.password;}
    public String getAccountType(){ return this.accountType;}
    public CreditCard getCreditCard(){ return this.creditCard; }

    //Setters
    public void setName(String newName){
        this.name = newName;
    }

    public void setId(int newID){ this.id = newID; }

    public void setEmail(String newEmail){
        this.email = newEmail;
    }

    public void setAddress(String newAddress){
        this.address = newAddress;
    }

    public void setPhoneNumber(String newPhoneNumber){
        this.phoneNumber = newPhoneNumber;
    }

    public void setPassword(String newPassword){
        this.password = newPassword;
    }

    public void setAccountType(String newAccountType){
        this.accountType = newAccountType;
    }

    public void setCreditCard(CreditCard newCard){ this.creditCard = newCard; }

}

