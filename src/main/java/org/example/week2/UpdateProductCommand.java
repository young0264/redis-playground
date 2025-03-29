package org.example.week2;

public record UpdateProductCommand(
        String keyId,
        Product updatedProduct
) {
}
