package com.example.copilot.rag.parser;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class MarkdownDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String fileType) {
        return "MARKDOWN".equalsIgnoreCase(fileType);
    }

    @Override
    public ParsedDocument parse(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        return new ParsedDocument(content, "{\"format\":\"markdown\"}");
    }
}
