package id.ac.ui.cs.advprog.groupproject.enums;

public enum TransactionType{
    TOP_UP, // User menambah saldo
    // TODO: implement payment
    PAYMENT, // Saldo dipotong ketika checkout
    // TODO: implement refund
    REFUND, // Saldo refund ketika order dibatalkan
    // TODO: implement withdrawal
    WITHDRAWAL // Jastiper tarik saldo ke rekening
}