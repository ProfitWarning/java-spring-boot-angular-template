package net.profitwarning.api.service;

import net.profitwarning.api.dto.CreateMessageCommand;
import net.profitwarning.api.dto.MessageResponse;
import net.profitwarning.api.model.TestMessage;
import net.profitwarning.api.repository.TestMessageRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Objects;

@Service
public class TestMessageService {

    private final TestMessageRepository repository;

    TestMessageService(TestMessageRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "testMessages")
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages() {
        return repository.findAll().stream()
                .map(message -> new MessageResponse(message.getId(), message.getContent(), message.getCreatedAt()))
                .toList();
    }

    @Cacheable(value = "testMessages", key = "#id")
    @Transactional(readOnly = true)
    public Optional<MessageResponse> getMessageById(@NonNull Long id) {
        Objects.requireNonNull(id, "ID must not be null");
        return repository.findById(id)
                .map(message -> new MessageResponse(message.getId(), message.getContent(), message.getCreatedAt()));
    }

    @CacheEvict(value = "testMessages", allEntries = true)
    @Transactional
    public MessageResponse saveMessage(CreateMessageCommand command) {
        TestMessage testMessage = new TestMessage();
        testMessage.setContent(command.content());
        TestMessage saved = repository.save(testMessage);
        return new MessageResponse(saved.getId(), saved.getContent(), saved.getCreatedAt());
    }
}
