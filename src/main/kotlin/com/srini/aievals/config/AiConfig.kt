package com.srini.aievals.config

import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.document.Document
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.vectorstore.SimpleVectorStore
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration
class AiConfig {

    private val log = LoggerFactory.getLogger(AiConfig::class.java)

    @Bean
    fun vectorStore(embeddingModel: EmbeddingModel): VectorStore {
        return SimpleVectorStore.builder(embeddingModel).build()
    }

    @Bean
    fun chatClient(chatModel: ChatModel): ChatClient {
        return ChatClient.builder(chatModel)
            .defaultSystem("You are a helpful customer support agent. Answer questions based only on the provided context. If you don't know the answer, say so clearly.")
            .build()
    }

    @Bean
    fun loadKnowledgeBase(vectorStore: VectorStore): CommandLineRunner {
        return CommandLineRunner {
            log.info("Loading FAQ knowledge base into vector store...")

            val resource = ClassPathResource("faq-knowledge-base.txt")
            val content = resource.getContentAsString(Charsets.UTF_8)

            // Split by separator and create documents
            val documents = content.split("---")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .mapIndexed { index, section ->
                    // Extract the policy name from the first line
                    val policyName = section.lines().first().trim().removeSuffix(":")
                    Document(
                        section,
                        mapOf("source" to "faq-knowledge-base", "policy" to policyName, "index" to index.toString())
                    )
                }

            vectorStore.add(documents)
            log.info("Loaded ${documents.size} FAQ documents into vector store")
        }
    }
}
