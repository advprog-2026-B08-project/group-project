package id.ac.ui.cs.advprog.groupproject.wallet.enums;

public enum TransactionType {
    TOP_UP,     // User menambah saldo
    DEBIT,      // Saldo dipotong ketika checkout
    REFUND,     // Saldo refund ketika order dibatalkan
    WITHDRAWAL, // Jastiper tarik saldo ke rekening
    CREDIT      // Jastiper menerima pendapatan dari order yang selesai
}