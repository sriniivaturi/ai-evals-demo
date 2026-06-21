package com.srini.aievals.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.stereotype.Service
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.vectorstore.VectorStore

@Service
class FaqAgentService(
    private val chatClient: ChatClient,
    private val vectorStore: VectorStore
) {

    /**
     * Ask the FAQ agent a question.
     * Returns the ChatResponse so we can extract both the answer and retrieved documents for evaluation.
     */
    fun ask(question: String): ChatResponse {
        return chatClient.prompt()
            .advisors(QuestionAnswerAdvisor.builder(vectorStore).build()
            )
            .user(question)
            .call()
            .chatResponse()!!
    }

    /**
     * Simple wrapper that returns just the answer text.
     */
    fun getAnswer(question: String): String {
        return ask(question).result.output.text ?: "No response generated"
    }
}
