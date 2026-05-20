package com.lovepreet.wallet_service.service;

import com.lovepreet.wallet_service.dto.WalletRequest;
import com.lovepreet.wallet_service.dto.WalletResponse;
import com.lovepreet.wallet_service.entity.Wallet;
import com.lovepreet.wallet_service.enums.OperationType;
import com.lovepreet.wallet_service.exception.InsufficientBalanceException;
import com.lovepreet.wallet_service.exception.WalletNotFoundException;
import com.lovepreet.wallet_service.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional(readOnly = true)
    public WalletResponse getBalance(UUID walletId) {
        if (walletId == null) {
            throw new IllegalArgumentException("Wallet ID must not be null");
        }
        log.info("Fetching balance for wallet ID: {}", walletId);
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with ID: " + walletId));
        return new WalletResponse(wallet.getId(), wallet.getBalance());
    }

    @Transactional
    public WalletResponse processOperation(WalletRequest request) {

        if (request.getWalletId() == null) {
            throw new IllegalArgumentException("walletId must not be null");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        if (request.getOperationType() == null) {
            throw new IllegalArgumentException("operationType must be DEPOSIT or WITHDRAW");
        }

        log.info("Processing transaction operation {} of amount {} on wallet {}",
                request.getOperationType(), request.getAmount(), request.getWalletId());

        Wallet wallet = walletRepository.findByIdWithLock(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with ID: " + request.getWalletId()));

        if (request.getOperationType() == OperationType.DEPOSIT) {
            wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        } else if (request.getOperationType() == OperationType.WITHDRAW) {
            if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException("Insufficient balance in wallet: " + request.getWalletId());
            }
            wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        }

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Successfully completed operation {} for wallet {}. New balance: {}",
                request.getOperationType(), savedWallet.getId(), savedWallet.getBalance());

        return new WalletResponse(savedWallet.getId(), savedWallet.getBalance());
    }
}
