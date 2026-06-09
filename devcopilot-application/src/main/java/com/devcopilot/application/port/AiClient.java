package com.devcopilot.application.port;

import java.util.List;

public interface AiClient {

    List<String> streamAnswer(String question, List<String> contexts);

    String complete(String instruction, String content);
}
