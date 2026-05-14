package com.portfolio.shared.constant;

public final class CorrelationConstants {

    private CorrelationConstants() {
    }

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String TRACE_ID_MDC_KEY = "traceId";
}
