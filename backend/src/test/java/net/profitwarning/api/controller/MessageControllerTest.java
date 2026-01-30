package net.profitwarning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.profitwarning.api.dto.CreateMessageCommand;
import net.profitwarning.api.dto.MessageResponse;
import net.profitwarning.api.service.TestMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@SuppressWarnings("null") // Suppress false-positive null warnings from Eclipse JDT
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TestMessageService testMessageService;

    @Test
    void shouldGetMessages() throws Exception {
        Instant now = Instant.now();
        List<MessageResponse> messages = List.of(
                new MessageResponse(1L, "Message 1", now),
                new MessageResponse(2L, "Message 2", now)
        );
        when(testMessageService.getMessages()).thenReturn(messages);

        mockMvc.perform(get("/api/v1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].content").value("Message 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].content").value("Message 2"));

        verify(testMessageService).getMessages();
    }

    @Test
    void shouldSaveMessage() throws Exception {
        String messageContent = "HelloTest";
        CreateMessageCommand command = new CreateMessageCommand(messageContent);

        MessageResponse savedMessage = new MessageResponse(1L, messageContent, Instant.now());
        when(testMessageService.saveMessage(any(CreateMessageCommand.class))).thenReturn(savedMessage);

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value(messageContent));

        verify(testMessageService).saveMessage(any(CreateMessageCommand.class));
    }

    @Test
    void shouldGetMessageById() throws Exception {
        MessageResponse message = new MessageResponse(1L, "Test Message", Instant.now());
        when(testMessageService.getMessageById(1L)).thenReturn(Optional.of(message));

        mockMvc.perform(get("/api/v1/messages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Test Message"));

        verify(testMessageService).getMessageById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenMessageDoesNotExist() throws Exception {
        when(testMessageService.getMessageById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/messages/999"))
                .andExpect(status().isNotFound());

        verify(testMessageService).getMessageById(999L);
    }
}
