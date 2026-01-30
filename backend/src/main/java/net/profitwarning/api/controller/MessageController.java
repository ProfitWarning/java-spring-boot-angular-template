package net.profitwarning.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.profitwarning.api.dto.CreateMessageCommand;
import net.profitwarning.api.dto.MessageResponse;
import net.profitwarning.api.service.TestMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@Tag(name = "Messages", description = "Endpoints for managing test messages")
class MessageController {

    private final TestMessageService testMessageService;

    MessageController(TestMessageService testMessageService) {
        this.testMessageService = testMessageService;
    }

    @GetMapping
    @Operation(summary = "Get all messages", description = "Retrieves a list of all messages stored in the database")
    ResponseEntity<List<MessageResponse>> getMessages() {
        return ResponseEntity.ok(testMessageService.getMessages());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get message by ID", description = "Retrieves a message by its ID")
    ResponseEntity<MessageResponse> getMessageById(@PathVariable Long id) {
        return testMessageService.getMessageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Save a message", description = "Saves a new message to the database")
    ResponseEntity<MessageResponse> saveMessage(@RequestBody @Valid CreateMessageCommand command) {
        MessageResponse response = testMessageService.saveMessage(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
