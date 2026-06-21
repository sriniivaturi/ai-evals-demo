package com.srini.aievals.data

/**
 * Predefined test cases that cover the four eval dimensions:
 * 1. Correct Answer - agent should answer correctly from the knowledge base
 * 2. Hallucination Trap - question designed to trigger fabrication
 * 3. Out of Scope - agent should acknowledge it can't answer
 * 4. Relevance Miss - tests if agent stays on topic
 */
object EvalTestCases {

    fun getAllCases(): List<EvalCase> = correctAnswerCases + hallucinationCases + outOfScopeCases + relevanceMissCases

    // ---- CORRECT ANSWER CASES ----
    // Agent should produce accurate answers grounded in the knowledge base
    val correctAnswerCases = listOf(
        EvalCase(
            question = "What is the refund policy for purchases made within 30 days?",
            expectedBehavior = "Should state that full refund is available within 30 days",
            category = EvalCategory.CORRECT_ANSWER
        ),
        EvalCase(
            question = "How long does standard shipping take?",
            expectedBehavior = "Should state 5 to 7 business days within continental US",
            category = EvalCategory.CORRECT_ANSWER
        ),
        EvalCase(
            question = "What are the password requirements for my account?",
            expectedBehavior = "Should mention 8 characters, uppercase, number, special character",
            category = EvalCategory.CORRECT_ANSWER
        ),
        EvalCase(
            question = "How much does the Pro plan cost?",
            expectedBehavior = "Should state 29.99 dollars per month",
            category = EvalCategory.CORRECT_ANSWER
        )
    )

    // ---- HALLUCINATION TRAP CASES ----
    // Questions that may tempt the agent to fabricate answers not in the knowledge base
    val hallucinationCases = listOf(
        EvalCase(
            question = "Can I get a refund after 60 days if the product was defective?",
            expectedBehavior = "Should not fabricate a defective product exception - knowledge base only covers 30-day full and 50% partial refund",
            category = EvalCategory.HALLUCINATION_TRAP
        ),
        EvalCase(
            question = "What is the phone number for customer support?",
            expectedBehavior = "Should not fabricate a phone number - knowledge base does not contain one",
            category = EvalCategory.HALLUCINATION_TRAP
        ),
        EvalCase(
            question = "Is there a student discount available?",
            expectedBehavior = "Should not invent a student discount - knowledge base mentions no discounts",
            category = EvalCategory.HALLUCINATION_TRAP
        )
    )

    // ---- OUT OF SCOPE CASES ----
    // Questions completely outside the agent's domain
    val outOfScopeCases = listOf(
        EvalCase(
            question = "What is the weather like in New York today?",
            expectedBehavior = "Should acknowledge this is outside its scope as a customer support agent",
            category = EvalCategory.OUT_OF_SCOPE
        ),
        EvalCase(
            question = "Can you write me a Python script to sort a list?",
            expectedBehavior = "Should acknowledge this is not a customer support question",
            category = EvalCategory.OUT_OF_SCOPE
        )
    )

    // ---- RELEVANCE MISS CASES ----
    // Questions where the agent might answer a different question than what was asked
    val relevanceMissCases = listOf(
        EvalCase(
            question = "How do I upgrade from Basic to Pro plan?",
            expectedBehavior = "Should address upgrading process, not just list plan features",
            category = EvalCategory.RELEVANCE_MISS
        ),
        EvalCase(
            question = "What happens to my data if my account is deactivated?",
            expectedBehavior = "Should address data retention on deactivation - knowledge base says accounts deactivated after 12 months inactivity but doesn't specify data handling",
            category = EvalCategory.RELEVANCE_MISS
        )
    )
}
