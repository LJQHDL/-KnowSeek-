package com.example.copilot.rag.parser;

import java.io.IOException;
import java.nio.file.Path;

public interface DocumentParser {

    boolean supports(String fileType);

    ParsedDocument parse(Path filePath) throws IOException;
}
