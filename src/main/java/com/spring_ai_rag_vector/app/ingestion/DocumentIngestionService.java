package com.spring_ai_rag_vector.app.ingestion;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource; // FIX 1: Use Spring's Resource, not MCP
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List; // FIX 2: Added missing import

@Service
public class DocumentIngestionService implements CommandLineRunner {

    @Value("classpath:pdf/WJAETS-2025-0723.pdf")
    private Resource resource;

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;  // inject this

    public DocumentIngestionService(VectorStore vectorStore, JdbcTemplate jdbcTemplate) {
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        
        // Check if already ingested
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM vector_store", Integer.class);
        
        if (count != null && count > 0) {
            System.out.println("Vector store already populated with " + count + " chunks. Skipping ingestion.");
            return;
        }

        if (!resource.exists()) {
            System.out.println("PDF file not found!");
            return;
        }

        System.out.println("Starting ingestion...");
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
        TextSplitter textSplitter = TokenTextSplitter.builder()
                .withChunkSize(400)
                .withMinChunkSizeChars(100)
                .withKeepSeparator(true)
                .build();

        List<Document> documents = tikaDocumentReader.read();
        List<Document> splitDocuments = textSplitter.split(documents);
        vectorStore.accept(splitDocuments);
        System.out.println("Ingestion complete: " + splitDocuments.size() + " chunks stored.");
    }
}
