package com.spring_ai_rag_vector.app.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final OllamaChatModel ollamaChatModel;
    private final VectorStore vectorStore;

    public ChatController(OllamaChatModel ollamaChatModel, VectorStore vectorStore) {
        this.ollamaChatModel = ollamaChatModel;
        this.vectorStore = vectorStore;
    }

    @PostMapping
    public String chat(@RequestBody String message) {

        String strictSystemText = """
                You are a strict technical assistant.
                Use ONLY the provided CONTEXT to answer the QUESTION.
                If the answer is not contained within the CONTEXT, say: 
                "I'm sorry, but that information is not available in the uploaded documents."
                Do not use any outside knowledge.
                """;

        return ChatClient.builder(ollamaChatModel)
                .build()
                .prompt()
                .system(strictSystemText)
                .advisors(
                    QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                            .topK(3)
                            .similarityThreshold(0.75)
                            .build())
                        .build()
                )
                .user(message)
                .call()
                .content();
    }
}
