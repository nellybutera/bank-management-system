package com.bank_management_system.customers;

public abstract class Customer {
    private final String customerId;
    private String name;
    private int age;
    private String contact;
    private String address;

    private static int customerCounter = 1;

    public Customer(String name, int age, String contact, String address){
        this.customerId = String.format("ACC%03d", customerCounter++);
        this.name = name;
        this.age = age;
        this.contact = contact;
        this.address = address;
    }

    public String getCustomerId(){ return customerId; }

    public String getName(){ return name; }
    public void setName(String fullname){
        name = fullname;
    }

    public int getAge(){ return age; }
    public void setAge(int userAge){
        age = userAge;
    }

    public String getContact(){ return contact; }
    public void setContact(String phone){
        contact = phone;
    }

    public String getAddress(){ return address; }
    public void setAddress(String location){
        address = location;
    }

    public abstract void displayCustomerDetails();

    public abstract String getCustomerType();

    
}
