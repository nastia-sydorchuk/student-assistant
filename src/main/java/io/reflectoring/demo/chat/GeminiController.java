package io.reflectoring.demo.chat;

import io.reflectoring.demo.dto.ChatRequest;
import io.reflectoring.demo.dto.ChatResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        System.out.println("JAVA RECEIVED QUERY: " + request.getQuery());
        return geminiService.callGemini(request);
    }
}
