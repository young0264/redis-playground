package org.example.redis.week2;

public record UpdateProductCommand(
        String keyId,
        Product updatedProduct
) {
}
