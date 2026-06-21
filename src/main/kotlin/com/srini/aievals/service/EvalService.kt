package com.srini.aievals.service

import com.srini.aievals.data.EvalCase
import com.srini.aievals.data.EvalResult
import com.srini.aievals.data.EvalSummary
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator
import org.springframework.ai.chat.evaluation.RelevancyEvaluator
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.document.Document
import org.springframework.ai.evaluation.EvaluationRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service

@Service
class EvalService(
    private val chatClient: ChatClient,
    private val chatModel: ChatModel,
    private val vectorStore: VectorStore
) {

    private val log = LoggerFactory.getLogger(EvalService::class.java)

    /**
     * Runs the full eval suite against a list of test cases.
     */
    fun runEvalSuite(testCases: List<EvalCase>): EvalSummary {
        val results = testCases.map { testCase ->
            log.info("Evaluating: ${testCase.question}")
            evaluateSingleCase(testCase)
        }

        val totalCases = results.size
        val relevancyPassed = results.count { it.relevancyPassed }
        val factCheckPassed = results.count { it.factCheckPassed }

        return EvalSummary(
            totalCases = totalCases,
            relevancyPassed = relevancyPassed,
            relevancyFailed = totalCases - relevancyPassed,
            factCheckPassed = factCheckPassed,
            factCheckFailed = totalCases - factCheckPassed,
            results = results
        )
    }

    /**
     * Evaluates a single question against both RelevancyEvaluator and FactCheckingEvaluator.
     */
    fun evaluateSingleCase(testCase: EvalCase): EvalResult {
        // Step 1: Get the agent response with RAG context
        val chatResponse: ChatResponse = chatClient.prompt()
            .advisors(QuestionAnswerAdvisor.builder(vectorStore).build()
            )
            .user(testCase.question)
            .call()
            .chatResponse()!!

        val answer = chatResponse.result.output.text ?: "No response"

        // Step 2: Extract retrieved documents from the RAG context
        @Suppress("UNCHECKED_CAST")
        val retrievedDocs: List<Document> = chatResponse.metadata
            .get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS) as? List<Document>
            ?: emptyList()

        // Step 3: Build EvaluationRequest
        val evalRequest = EvaluationRequest(testCase.question, retrievedDocs, answer)

        // Step 4: Run RelevancyEvaluator
        val chatClientBuilder = ChatClient.builder(chatModel)
        val relevancyEvaluator = RelevancyEvaluator(chatClientBuilder)
        val relevancyResult = relevancyEvaluator.evaluate(evalRequest)

        // Step 5: Run FactCheckingEvaluator
        val factCheckEvaluator = FactCheckingEvaluator.builder(ChatClient.builder(chatModel)).build()
        val factCheckResult = factCheckEvaluator.evaluate(evalRequest)

        log.info(
            "  Question: ${testCase.question}\n" +
            "  Answer: ${answer.take(100)}...\n" +
            "  Relevancy: ${if (relevancyResult.isPass) "PASS" else "FAIL"}\n" +
            "  FactCheck: ${if (factCheckResult.isPass) "PASS" else "FAIL"}"
        )

        return EvalResult(
            question = testCase.question,
            expectedBehavior = testCase.expectedBehavior,
            actualAnswer = answer,
            relevancyPassed = relevancyResult.isPass,
            factCheckPassed = factCheckResult.isPass,
            category = testCase.category
        )
    }
}
