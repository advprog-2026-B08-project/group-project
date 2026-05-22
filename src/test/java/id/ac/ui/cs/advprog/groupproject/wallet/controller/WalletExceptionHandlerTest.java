package id.ac.ui.cs.advprog.groupproject.wallet.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

class WalletExceptionHandlerTest {

    private final WalletExceptionHandler handler = new WalletExceptionHandler();

    @Test
    void handleIllegalArgument_returnsBadRequest() {
        MockHttpServletRequest request = request("/api/wallet/top-up/123");

        WalletExceptionHandler.ApiError error = handler
                .handleIllegalArgument(new IllegalArgumentException("Amount must be greater than zero"), request)
                .getBody();

        assertNotNull(error);
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.status());
        assertEquals("Amount must be greater than zero", error.message());
        assertEquals("/api/wallet/top-up/123", error.path());
    }

    @Test
    void handleIllegalState_returnsConflict() {
        MockHttpServletRequest request = request("/api/wallet/refund");

        WalletExceptionHandler.ApiError error = handler
                .handleIllegalState(new IllegalStateException("Transaction already processed"), request)
                .getBody();

        assertNotNull(error);
        assertEquals(HttpStatus.CONFLICT.value(), error.status());
        assertEquals("Transaction already processed", error.message());
    }

    @Test
    void handleValidation_usesFieldErrorMessage() throws Exception {
        MockHttpServletRequest request = request("/api/wallet/top-up/123");
        MethodParameter parameter = methodParameter("dummy", String.class);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "payload");
        bindingResult.addError(new FieldError("payload", "amount", "Amount must be greater than zero"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        WalletExceptionHandler.ApiError error = handler.handleValidation(exception, request).getBody();

        assertNotNull(error);
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.status());
        assertEquals("Amount must be greater than zero", error.message());
    }

    @Test
    void handleValidation_fallsBackToDefaultMessage() throws Exception {
        MockHttpServletRequest request = request("/api/wallet/top-up/123");
        MethodParameter parameter = methodParameter("dummy", String.class);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "payload");
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        WalletExceptionHandler.ApiError error = handler.handleValidation(exception, request).getBody();

        assertNotNull(error);
        assertEquals("Validation failed", error.message());
    }

    @Test
    void handleNotReadable_returnsBadRequest() {
        MockHttpServletRequest request = request("/api/wallet/top-up/123");
        HttpInputMessage inputMessage = mock(HttpInputMessage.class);

        WalletExceptionHandler.ApiError error = handler
                .handleNotReadable(
                        new org.springframework.http.converter.HttpMessageNotReadableException(
                                "bad json", new RuntimeException("malformed"), inputMessage),
                        request)
                .getBody();

        assertNotNull(error);
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.status());
        assertEquals("Invalid request body", error.message());
    }

    @Test
    void handleResponseStatus_usesReasonAndStatus() {
        MockHttpServletRequest request = request("/api/wallet/balance/123");

        WalletExceptionHandler.ApiError error = handler
                .handleResponseStatus(new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"), request)
                .getBody();

        assertNotNull(error);
        assertEquals(HttpStatus.NOT_FOUND.value(), error.status());
        assertEquals("Wallet not found", error.message());
    }

    @Test
    void handleResponseStatus_fallsBackToExceptionMessage() {
        MockHttpServletRequest request = request("/api/wallet/balance/123");

        WalletExceptionHandler.ApiError error = handler
                .handleResponseStatus(new ResponseStatusException(HttpStatus.NOT_FOUND), request)
                .getBody();

        assertNotNull(error);
        assertEquals(HttpStatus.NOT_FOUND.value(), error.status());
        assertEquals("404 NOT_FOUND", error.message());
    }

    private static MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        return request;
    }

    private static MethodParameter methodParameter(String methodName, Class<?> parameterType) throws Exception {
        Method method = WalletExceptionHandlerTest.class.getDeclaredMethod(methodName, parameterType);
        return new MethodParameter(method, 0);
    }

    @SuppressWarnings("unused")
    private void dummy(String value) {
    }
}