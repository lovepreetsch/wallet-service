package com.lovepreet.wallet_service.controller;

import com.lovepreet.wallet_service.dto.WalletRequest;
import com.lovepreet.wallet_service.dto.WalletResponse;
import com.lovepreet.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/wallet")
    public ResponseEntity<WalletResponse> processWalletOperation(@Valid @RequestBody WalletRequest request) {
        int maxAttempts = 5;
        int attempt = 0;
        long backoff = 50; // ms

        while (true) {
            try {
                WalletResponse response = walletService.processOperation(request);
                return ResponseEntity.ok(response);
            } catch (ConcurrencyFailureException | TransactionSystemException ex) {
                attempt++;
                if (attempt >= maxAttempts) {
                    throw ex;
                }
                try {
                    Thread.sleep(backoff * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
    }

    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<WalletResponse> getWalletBalance(@PathVariable UUID walletId) {
        WalletResponse response = walletService.getBalance(walletId);
        return ResponseEntity.ok(response);
    }
}
