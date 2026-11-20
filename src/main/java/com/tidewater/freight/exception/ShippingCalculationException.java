package com.tidewater.freight.exception;

public class ShippingCalculationException extends RuntimeException {

    protected ShippingCalculationException(String message) {
        super(message);
    }

    protected ShippingCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
