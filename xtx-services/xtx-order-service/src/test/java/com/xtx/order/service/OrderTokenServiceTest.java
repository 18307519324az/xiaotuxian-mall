package com.xtx.order.service;

import com.xtx.common.core.exception.BizException;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderTokenServiceTest {

    @Test
    void generateTokenShouldReturnNonNull() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        when(redisTemplate.opsForValue()).thenReturn(mock());
        OrderTokenService tokenService = new OrderTokenService(redisTemplate);

        String token = tokenService.generateToken(1L);

        assertNotNull(token);
        assertFalse(token.isBlank());
        verify(redisTemplate).opsForValue();
    }

    @Test
    void firstUseShouldSucceed() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        when(redisTemplate.delete(anyString())).thenReturn(true);
        OrderTokenService tokenService = new OrderTokenService(redisTemplate);

        assertDoesNotThrow(() -> tokenService.validateAndConsumeToken(1L, "valid-token"));
        verify(redisTemplate).delete("xtx:order:token:1:valid-token");
    }

    @Test
    void duplicateUseShouldThrow() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        when(redisTemplate.delete(anyString())).thenReturn(false);
        OrderTokenService tokenService = new OrderTokenService(redisTemplate);

        BizException ex = assertThrows(BizException.class,
                () -> tokenService.validateAndConsumeToken(1L, "used-token"));
        assertEquals(40020, ex.getCode());
    }

    @Test
    void nullTokenShouldThrow() {
        OrderTokenService tokenService = new OrderTokenService(mock(RedisTemplate.class));

        BizException ex = assertThrows(BizException.class,
                () -> tokenService.validateAndConsumeToken(1L, null));
        assertEquals(40021, ex.getCode());
    }

    @Test
    void emptyTokenShouldThrow() {
        OrderTokenService tokenService = new OrderTokenService(mock(RedisTemplate.class));

        assertThrows(BizException.class, () -> tokenService.validateAndConsumeToken(1L, ""));
        assertThrows(BizException.class, () -> tokenService.validateAndConsumeToken(1L, "   "));
    }
}
