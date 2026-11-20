package com.tidewater.freight.exception;

import com.tidewater.freight.validation.ValidationError;

import java.util.List;

public final class InvalidShipmentException extends ShippingCalculationException {

    private final List<ValidationError> errors;

    public InvalidShipmentException(List<ValidationError> errors) {
        super("Invalid shipment: " + errors);
        this.errors = List.copyOf(errors);
    }

    public List<ValidationError> errors() {
        return errors;
    }
}
