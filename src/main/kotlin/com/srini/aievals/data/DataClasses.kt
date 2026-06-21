package com.srini.aievals.data

/**
 * Represents a single test case for evaluation.
 */
data class EvalCase(
    val question: String,
    val expectedBehavior: String,
    val category: EvalCategory
)

/**
 * Categories of test cases to demonstrate different eval dimensions.
 */
enum class EvalCategory {
    CORRECT_ANSWER,       // Should produce a correct, relevant answer
    HALLUCINATION_TRAP,   // Likely to trigger fabrication
    OUT_OF_SCOPE,         // Agent should refuse or acknowledge it can't answer
    RELEVANCE_MISS        // Tests if the answer addresses the actual question asked
}

/**
 * Result of evaluating a single test case.
 */
data class EvalResult(
    val question: String,
    val expectedBehavior: String,
    val actualAnswer: String,
    val relevancyPassed: Boolean,
    val factCheckPassed: Boolean,
    val category: EvalCategory
)

/**
 * Summary of the entire eval suite run.
 */
data class EvalSummary(
    val totalCases: Int,
    val relevancyPassed: Int,
    val relevancyFailed: Int,
    val factCheckPassed: Int,
    val factCheckFailed: Int,
    val results: List<EvalResult>
)
