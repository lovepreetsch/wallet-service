package com.lovepreet.wallet_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovepreet.wallet_service.controller.WalletController;
import com.lovepreet.wallet_service.dto.WalletRequest;
import com.lovepreet.wallet_service.dto.WalletResponse;
import com.lovepreet.wallet_service.enums.OperationType;
import com.lovepreet.wallet_service.exception.InsufficientBalanceException;
import com.lovepreet.wallet_service.exception.WalletNotFoundException;
import com.lovepreet.wallet_service.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetWalletBalance_Success() throws Exception {
        UUID walletId = UUID.randomUUID();
        WalletResponse response = new WalletResponse(walletId, new BigDecimal("100.00"));

        when(walletService.getBalance(walletId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/wallets/" + walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(walletId.toString()))
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    public void testGetWalletBalance_NotFound() throws Exception {
        UUID walletId = UUID.randomUUID();

        when(walletService.getBalance(walletId))
                .thenThrow(new WalletNotFoundException("Wallet not found with ID: " + walletId));

        mockMvc.perform(get("/api/v1/wallets/" + walletId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Wallet not found with ID: " + walletId));
    }

    @Test
    public void testDeposit_Success() throws Exception {
        UUID walletId = UUID.randomUUID();
        WalletRequest request = new WalletRequest(walletId, OperationType.DEPOSIT, new BigDecimal("500.00"));
        WalletResponse response = new WalletResponse(walletId, new BigDecimal("1500.00"));

        when(walletService.processOperation(any(WalletRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(walletId.toString()))
                .andExpect(jsonPath("$.balance").value(1500.00));
    }

    @Test
    public void testWithdraw_Success() throws Exception {
        UUID walletId = UUID.randomUUID();
        WalletRequest request = new WalletRequest(walletId, OperationType.WITHDRAW, new BigDecimal("200.00"));
        WalletResponse response = new WalletResponse(walletId, new BigDecimal("800.00"));

        when(walletService.processOperation(any(WalletRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(walletId.toString()))
                .andExpect(jsonPath("$.balance").value(800.00));
    }

    @Test
    public void testWithdraw_InsufficientFunds() throws Exception {
        UUID walletId = UUID.randomUUID();
        WalletRequest request = new WalletRequest(walletId, OperationType.WITHDRAW, new BigDecimal("2000.00"));

        when(walletService.processOperation(any(WalletRequest.class)))
                .thenThrow(new InsufficientBalanceException("Insufficient balance in wallet: " + walletId));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Insufficient balance in wallet: " + walletId));
    }

    @Test
    public void testProcessWalletOperation_InvalidValidation() throws Exception {
        UUID walletId = UUID.randomUUID();
        WalletRequest request = new WalletRequest(walletId, OperationType.DEPOSIT, new BigDecimal("-50.00"));

        when(walletService.processOperation(any(WalletRequest.class)))
                .thenThrow(new IllegalArgumentException("amount must be greater than 0"));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("amount: must be greater than or equal to 0.01"));
    }
}
