package com.challenge.notification.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MDCRequestFilterTest {

    private final MDCRequestFilter filter = new MDCRequestFilter();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldUseIncomingCorrelationIdHeader() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationConstants.CORRELATION_ID_HEADER, "incoming-correlation-id");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // when
        filter.doFilter(request, response, filterChain);

        // assert
        assertThat(response.getHeader(CorrelationConstants.CORRELATION_ID_HEADER))
                .isEqualTo("incoming-correlation-id");

        assertThat(MDC.get(CorrelationConstants.CORRELATION_ID_MDC_KEY))
                .isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderIsMissing() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // when
        filter.doFilter(request, response, filterChain);

        // assert
        assertThat(response.getHeader(CorrelationConstants.CORRELATION_ID_HEADER))
                .isNotBlank();

        assertThat(MDC.get(CorrelationConstants.CORRELATION_ID_MDC_KEY))
                .isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldExposeCorrelationIdInMdcDuringFilterChainExecution() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationConstants.CORRELATION_ID_HEADER, "chain-correlation-id");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        ArgumentCaptor<MockHttpServletRequest> requestCaptor =
                ArgumentCaptor.forClass(MockHttpServletRequest.class);

        // when
        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            assertThat(MDC.get(CorrelationConstants.CORRELATION_ID_MDC_KEY))
                    .isEqualTo("chain-correlation-id");

            filterChain.doFilter(servletRequest, servletResponse);
        });

        // assert
        assertThat(MDC.get(CorrelationConstants.CORRELATION_ID_MDC_KEY))
                .isNull();

        verify(filterChain).doFilter(requestCaptor.capture(), org.mockito.Mockito.any());
        assertThat(requestCaptor.getValue()).isSameAs(request);
    }
}
