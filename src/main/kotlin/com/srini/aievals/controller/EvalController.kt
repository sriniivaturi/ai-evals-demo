package com.srini.aievals.controller

import com.srini.aievals.data.EvalCase
import com.srini.aievals.data.EvalCategory
import com.srini.aievals.data.EvalResult
import com.srini.aievals.data.EvalSummary
import com.srini.aievals.data.EvalTestCases
import com.srini.aievals.service.EvalService
import com.srini.aievals.service.FaqAgentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class EvalController(
    private val faqAgentService: FaqAgentService,
    private val evalService: EvalService
) {

    /**
     * Ask the FAQ agent a question directly.
     * GET /ask?question=What is the refund policy?
     */
    @GetMapping("/ask")
    fun ask(@RequestParam question: String): Map<String, String> {
        val answer = faqAgentService.getAnswer(question)
        return mapOf("question" to question, "answer" to answer)
    }

    /**
     * Run the full predefined eval suite.
     * GET /eval/run
     */
    @GetMapping("/eval/run")
    fun runFullEvalSuite(): EvalSummary {
        return evalService.runEvalSuite(EvalTestCases.getAllCases())
    }

    /**
     * Run evals for a specific category only.
     * GET /eval/run/category?category=HALLUCINATION_TRAP
     */
    @GetMapping("/eval/run/category")
    fun runEvalByCategory(@RequestParam category: String): EvalSummary {
        val evalCategory = EvalCategory.valueOf(category.uppercase())
        val cases = EvalTestCases.getAllCases().filter { it.category == evalCategory }
        return evalService.runEvalSuite(cases)
    }

    /**
     * Evaluate a single custom question.
     * POST /eval/single
     * Body: { "question": "...", "expectedBehavior": "...", "category": "CORRECT_ANSWER" }
     */
    @PostMapping("/eval/single")
    fun evaluateSingle(@RequestBody evalCase: EvalCase): EvalResult {
        return evalService.evaluateSingleCase(evalCase)
    }
}
