package com.mingeso.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Parametros del calculo de costo (app.cost.*).
 *
 * @param taxRate IVA como fraccion, ej. 0.19 (variable TAX_RATE)
 */
@ConfigurationProperties(prefix = "app.cost")
public record CostProperties(BigDecimal taxRate) {
}
