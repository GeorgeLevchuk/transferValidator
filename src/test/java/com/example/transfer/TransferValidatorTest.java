package com.example.transfer;

import com.example.transfer.error.InvalidTransferException;
import com.example.transfer.error.TransferError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferValidatorTest {

    @Mock
    private AccountRepository accounts;

    @Mock
    private TransferLimitProvider limits;

    @InjectMocks
    private TransferValidator transferValidator;

    @Test
    void zeroAmount_throwsAmountNotPositive(){
        Currency rub = Currency.getInstance("RUB");
        TransferRequest request = new TransferRequest("A", "B",new BigDecimal("0"),rub);

        InvalidTransferException ex = assertThrows(
                InvalidTransferException.class,
                () -> transferValidator.validate(request)
        );

        assertEquals(TransferError.AMOUNT_NOT_POSITIVE,ex.getError());
    }

    @Test
    void negativeAmount_throwsAmountNotPositive(){
        Currency rub = Currency.getInstance("RUB");
        TransferRequest request = new TransferRequest("A", "B",new BigDecimal("-10"),rub);

        InvalidTransferException ex = assertThrows(
                InvalidTransferException.class,
                () -> transferValidator.validate(request)
        );

        assertEquals(TransferError.AMOUNT_NOT_POSITIVE,ex.getError());

    }

    @Test
    void nullRequest_throwsInvalidRequest() {


        InvalidTransferException ex = assertThrows(
                InvalidTransferException.class,
                () -> transferValidator.validate(null)
        );


        assertEquals(TransferError.INVALID_REQUEST,ex.getError());
    }

    @Test
    void nullAmount_throwsInvalidRequest(){
        Currency rub = Currency.getInstance("RUB");
        TransferRequest request = new TransferRequest("A", "B",null,rub);


        InvalidTransferException ex1 = assertThrows(
                InvalidTransferException.class,
                () -> transferValidator.validate(request)
        );

        assertEquals(TransferError.INVALID_REQUEST,ex1.getError());

    }

    @Test
    void nullFromAccountId_throwsInvalidRequest(){
        Currency rub = Currency.getInstance("RUB");
        TransferRequest request = new TransferRequest(null, "B",new BigDecimal("1"),rub);


        InvalidTransferException ex1 = assertThrows(
                InvalidTransferException.class,
                () -> transferValidator.validate(request)
        );

        assertEquals(TransferError.INVALID_REQUEST,ex1.getError());

    }

    @Test
    void nullToAccountId_throwsInvalidRequest(){
        Currency rub = Currency.getInstance("RUB");
        TransferRequest request = new TransferRequest("A", null,new BigDecimal("1"),rub);


        InvalidTransferException ex1 = assertThrows(
                InvalidTransferException.class,
                () -> transferValidator.validate(request)
        );

        assertEquals(TransferError.INVALID_REQUEST,ex1.getError());

    }

    @Test
    void nullCurrency_throwsInvalidRequest(){
        TransferRequest request = new TransferRequest("A", "B",new BigDecimal("1"),null);


        InvalidTransferException ex1 = assertThrows(
                InvalidTransferException.class,
                () -> transferValidator.validate(request)
        );

        assertEquals(TransferError.INVALID_REQUEST,ex1.getError());

    }

    @Test
    void validTransfer_passes(){
        Currency rub = Currency.getInstance("RUB");
        Account from = new Account("A", new BigDecimal("1000.00"), rub);
        Account to = new Account("B", new BigDecimal("0.00"), rub);
        TransferRequest request = new TransferRequest("A", "B",new BigDecimal("1"),rub);

        when(accounts.findById("A")).thenReturn(Optional.of(from));
        when(accounts.findById("B")).thenReturn(Optional.of(to));
        when(limits.getLimit("A")).thenReturn(new BigDecimal("5000.00"));

        assertDoesNotThrow(() -> transferValidator.validate(request));

        verify(accounts).findById("A");
        verify(accounts).findById("B");
        verify(limits).getLimit("A");
    }

    @Test
    void otherScale_throwsAmountScaleInvalid() {
        Currency rub = Currency.getInstance("RUB");
        TransferRequest request = new TransferRequest("A", "B", new BigDecimal("1.001"), rub);

        InvalidTransferException ex = assertThrows(
                InvalidTransferException.class,
                () -> transferValidator.validate(request)
        );

        assertEquals(TransferError.AMOUNT_SCALE_INVALID, ex.getError());
        verifyNoInteractions(accounts, limits);
    }

    @Test
    void otherCurrency_throwsCurrencyMismatch(){
        Currency rub = Currency.getInstance("RUB");
        Account from = new Account("A", new BigDecimal("1000.00"), rub);
        Account to = new Account("B", new BigDecimal("0.00"), rub);
        Currency other = Currency.getInstance("EUR");
        TransferRequest request = new TransferRequest("A", "B",new BigDecimal("1"),other);

        when(accounts.findById("A")).thenReturn(Optional.of(from));
        when(accounts.findById("B")).thenReturn(Optional.of(to));

        InvalidTransferException ex1 = assertThrows(
                InvalidTransferException.class,
                () -> transferValidator.validate(request)
        );

        assertEquals(TransferError.CURRENCY_MISMATCH,ex1.getError());
    }

    @Test
    void sameAccount_throwsSameAccount(){
        Currency rub = Currency.getInstance("RUB");
        TransferRequest request = new TransferRequest("A", "A",new BigDecimal("1"),rub);

        InvalidTransferException ex1 = assertThrows(
                InvalidTransferException.class,
                () -> transferValidator.validate(request)
        );

        assertEquals(TransferError.SAME_ACCOUNT,ex1.getError());
        verifyNoInteractions(accounts, limits);
    }

    @Test
    void senderNotFound_throwsAccountNotFound(){
        Currency rub = Currency.getInstance("RUB");
        TransferRequest request = new TransferRequest("A", "B",new BigDecimal("1"),rub);


        when(accounts.findById("A")).thenReturn(Optional.empty());

        InvalidTransferException ex1 = assertThrows(
                InvalidTransferException.class,
                () -> transferValidator.validate(request)
        );

        assertEquals(TransferError.ACCOUNT_NOT_FOUND,ex1.getError());
        verify(accounts, never()).findById("B");

    }

    @Test
    void receiverNotFound_throwsAccountNotFound(){
        Currency rub = Currency.getInstance("RUB");
        Account from = new Account("A", new BigDecimal("1000.00"), rub);
        TransferRequest request = new TransferRequest("A", "B",new BigDecimal("1"),rub);

        when(accounts.findById("A")).thenReturn(Optional.of(from));
        when(accounts.findById("B")).thenReturn(Optional.empty());

        InvalidTransferException ex1 = assertThrows(
                InvalidTransferException.class,
                () -> transferValidator.validate(request)
        );

        assertEquals(TransferError.ACCOUNT_NOT_FOUND,ex1.getError());
        verify(accounts).findById("A");
        verify(accounts).findById("B");
    }

    @Test
    void amountOverLimitByCent_throwsLimitExceeded(){
        Currency rub = Currency.getInstance("RUB");
        Account from = new Account("A", new BigDecimal("1000.00"), rub);
        Account to = new Account("B", new BigDecimal("0.00"), rub);
        TransferRequest request = new TransferRequest("A", "B",new BigDecimal("100.01"),rub);

        when(accounts.findById("A")).thenReturn(Optional.of(from));
        when(accounts.findById("B")).thenReturn(Optional.of(to));
        when(limits.getLimit("A")).thenReturn(new BigDecimal("100.00"));

        InvalidTransferException ex1 = assertThrows(
                InvalidTransferException.class,
                () -> transferValidator.validate(request)
        );

        assertEquals(TransferError.LIMIT_EXCEEDED,ex1.getError());
        verify(limits).getLimit("A");
    }

    @Test
    void insufficientBalance_throwsInsufficientFunds(){
        Currency rub = Currency.getInstance("RUB");
        Account from = new Account("A", new BigDecimal("99.99"), rub);
        Account to = new Account("B", new BigDecimal("0.00"), rub);
        TransferRequest request = new TransferRequest("A", "B",new BigDecimal("100.00"),rub);

        when(accounts.findById("A")).thenReturn(Optional.of(from));
        when(accounts.findById("B")).thenReturn(Optional.of(to));
        when(limits.getLimit("A")).thenReturn(new BigDecimal("10000"));

        InvalidTransferException ex1 = assertThrows(
                InvalidTransferException.class,
                () -> transferValidator.validate(request)
        );

        assertEquals(TransferError.INSUFFICIENT_FUNDS,ex1.getError());
        verify(limits).getLimit("A");
    }

    @Test
    void amountEqualsLimit_passes(){
        Currency rub = Currency.getInstance("RUB");
        Account from = new Account("A", new BigDecimal("1000.99"), rub);
        Account to = new Account("B", new BigDecimal("0.00"), rub);
        TransferRequest request = new TransferRequest("A", "B",new BigDecimal("100.00"),rub);

        when(accounts.findById("A")).thenReturn(Optional.of(from));
        when(accounts.findById("B")).thenReturn(Optional.of(to));
        when(limits.getLimit("A")).thenReturn(new BigDecimal("100.00"));

        assertDoesNotThrow(() -> transferValidator.validate(request));

        verify(accounts).findById("A");
        verify(accounts).findById("B");
        verify(limits).getLimit("A");
    }

    @Test
    void amountEqualsBalance_passes(){
        Currency rub = Currency.getInstance("RUB");
        Account from = new Account("A", new BigDecimal("1000.99"), rub);
        Account to = new Account("B", new BigDecimal("0.00"), rub);
        TransferRequest request = new TransferRequest("A", "B",new BigDecimal("1000.99"),rub);

        when(accounts.findById("A")).thenReturn(Optional.of(from));
        when(accounts.findById("B")).thenReturn(Optional.of(to));
        when(limits.getLimit("A")).thenReturn(new BigDecimal("10000.99"));

        assertDoesNotThrow(() -> transferValidator.validate(request));

        verify(accounts).findById("A");
        verify(accounts).findById("B");
        verify(limits).getLimit("A");
    }

    @Test
    void receiverCurrencyDiffers_throwsCurrencyMismatch(){
        Currency rub = Currency.getInstance("RUB");
        Account from = new Account("A", new BigDecimal("1000.00"), rub);
        Currency other = Currency.getInstance("EUR");
        Account to = new Account("B", new BigDecimal("0.00"), other);
        TransferRequest request = new TransferRequest("A", "B",new BigDecimal("1"),rub);

        when(accounts.findById("A")).thenReturn(Optional.of(from));
        when(accounts.findById("B")).thenReturn(Optional.of(to));

        InvalidTransferException ex1 = assertThrows(
                InvalidTransferException.class,
                () -> transferValidator.validate(request)
        );

        assertEquals(TransferError.CURRENCY_MISMATCH,ex1.getError());
    }
}
