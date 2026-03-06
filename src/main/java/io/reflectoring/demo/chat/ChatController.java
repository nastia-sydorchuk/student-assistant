package io.reflectoring.demo.chat;

import io.reflectoring.demo.dto.ChatRequest;
import io.reflectoring.demo.dto.ChatResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
@CrossOrigin(origins = "http://localhost:4200")
public class ChatController {

    private final ChatService geminiService;

    public ChatController(ChatService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        System.out.println("JAVA RECEIVED QUERY: " + request.getQuery());
        return geminiService.callGemini(request);
    }
}
