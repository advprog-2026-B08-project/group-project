package id.ac.ui.cs.advprog.groupproject.wallet.enums;

public enum TransactionType{
    TOP_UP, // User menambah saldo
    // TODO: implement deduct on checkout
    DEBIT, // Saldo dipotong ketika checkout
    // TODO: implement refund
    REFUND, // Saldo refund ketika order dibatalkan
    // TODO: implement withdrawal
    WITHDRAWAL // Jastiper tarik saldo ke rekening
}