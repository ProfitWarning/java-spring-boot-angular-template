package net.profitwarning.api.service;

import net.profitwarning.api.dto.CreateMessageCommand;
import net.profitwarning.api.dto.MessageResponse;
import net.profitwarning.api.model.TestMessage;
import net.profitwarning.api.repository.TestMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableCaching
@TestPropertySource(properties = {
        "spring.cache.type=caffeine",
        "spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=10m"
})
@SuppressWarnings("null")
class TestMessageServiceCachingIntegrationTest {

    @MockitoBean
    private TestMessageRepository mockRepository;

    @Autowired
    private TestMessageService testMessageService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("testMessages").clear();
    }

    @Test
    void givenRedisCaching_whenGetMessagesTwice_thenRepositoryCalledOnce() {
        List<TestMessage> testMessages = List.of(
                createTestMessage(1L, "Message 1"),
                createTestMessage(2L, "Message 2")
        );
        given(mockRepository.findAll()).willReturn(testMessages);

        // First call - cache miss, should hit repository
        List<MessageResponse> firstCall = testMessageService.getMessages();
        // Second call - cache hit, should NOT hit repository
        List<MessageResponse> secondCall = testMessageService.getMessages();

        assertThat(firstCall).hasSize(2);
        assertThat(secondCall).hasSize(2);
        assertThat(firstCall).isEqualTo(secondCall);

        // Repository should only be called once due to caching
        verify(mockRepository, times(1)).findAll();
    }

    @Test
    void givenCachedMessages_whenSaveMessage_thenCacheEvicted() {
        List<TestMessage> testMessages = List.of(createTestMessage(1L, "Message 1"));
        given(mockRepository.findAll()).willReturn(testMessages);

        TestMessage savedMessage = createTestMessage(2L, "New Message");
        given(mockRepository.save(any(TestMessage.class))).willReturn(savedMessage);

        // First call - populates cache
        testMessageService.getMessages();
        verify(mockRepository, times(1)).findAll();

        // Save a new message - should evict cache
        testMessageService.saveMessage(new CreateMessageCommand("New Message"));

        // Get messages again - cache was evicted, should hit repository again
        testMessageService.getMessages();
        verify(mockRepository, times(2)).findAll();
    }

    @Test
    void givenRedisCaching_whenGetMessageByIdTwice_thenRepositoryCalledOnce() {
        Long messageId = 1L;
        TestMessage testMessage = createTestMessage(messageId, "Test Message");
        given(mockRepository.findById(messageId)).willReturn(Optional.of(testMessage));

        // First call - cache miss, should hit repository
        Optional<MessageResponse> firstCall = testMessageService.getMessageById(messageId);
        // Second call - cache hit, should NOT hit repository
        Optional<MessageResponse> secondCall = testMessageService.getMessageById(messageId);

        assertThat(firstCall).isPresent();
        assertThat(secondCall).isPresent();
        assertThat(firstCall.get()).isEqualTo(secondCall.get());

        // Repository should only be called once due to caching
        verify(mockRepository, times(1)).findById(messageId);
    }

    @Test
    void givenMultipleMessages_whenGetByDifferentIds_thenEachCachedSeparately() {
        TestMessage message1 = createTestMessage(1L, "Message 1");
        TestMessage message2 = createTestMessage(2L, "Message 2");
        given(mockRepository.findById(1L)).willReturn(Optional.of(message1));
        given(mockRepository.findById(2L)).willReturn(Optional.of(message2));

        // Get message 1 twice
        testMessageService.getMessageById(1L);
        testMessageService.getMessageById(1L);

        // Get message 2 twice
        testMessageService.getMessageById(2L);
        testMessageService.getMessageById(2L);

        // Each ID should only hit the repository once
        verify(mockRepository, times(1)).findById(1L);
        verify(mockRepository, times(1)).findById(2L);
    }

    private TestMessage createTestMessage(Long id, String message) {
        TestMessage testMessage = new TestMessage();
        testMessage.setId(id);
        testMessage.setContent(message);
        return testMessage;
    }
}

