# AI Evals Demo — Spring AI + Kotlin

A hands-on demonstration of evaluating LLM agent responses using Spring AI's built-in `RelevancyEvaluator` and `FactCheckingEvaluator`.

## What This Does

A simple **Customer Support FAQ Agent** backed by RAG (Retrieval-Augmented Generation):
- Loads FAQ policy documents into an in-memory `SimpleVectorStore`
- Answers questions using OpenAI GPT-4o with RAG context
- Evaluates responses for **relevancy** and **factual accuracy**
- Demonstrates 4 eval dimensions: correct answers, hallucination traps, out-of-scope, and relevance misses

## Stack

- **Spring Boot 3.4.4** + **Kotlin 2.1.0**
- **Spring AI 1.1.7** (stable GA)
- **OpenAI GPT-4o** (chat) + **text-embedding-3-small** (embeddings)
- **SimpleVectorStore** (in-memory — no external DB required)

## Prerequisites

- Java 17+
- An OpenAI API key

## Setup

1. **Set your OpenAI API key as an environment variable:**

```bash
export OPENAI_API_KEY=sk-your-key-here
```

2. **Generate the Gradle wrapper** (if not already present):

```bash
gradle wrapper --gradle-version 8.12
```

3. **Build and run:**

```bash
./gradlew bootRun
```

The app starts on `http://localhost:8080`. On startup it loads 5 FAQ documents into the vector store.

## API Endpoints

### Ask the FAQ Agent

```bash
curl "http://localhost:8080/ask?question=What%20is%20the%20refund%20policy"
```

### Run Full Eval Suite (11 test cases)

```bash
curl http://localhost:8080/eval/run
```

### Run Evals by Category

```bash
# Categories: CORRECT_ANSWER, HALLUCINATION_TRAP, OUT_OF_SCOPE, RELEVANCE_MISS
curl "http://localhost:8080/eval/run/category?category=HALLUCINATION_TRAP"
```

### Evaluate a Custom Question

```bash
curl -X POST http://localhost:8080/eval/single \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Do you offer lifetime subscriptions?",
    "expectedBehavior": "Should not fabricate a lifetime plan",
    "category": "HALLUCINATION_TRAP"
  }'
```

## Sample Output

```json
{
  "totalCases": 11,
  "relevancyPassed": 9,
  "relevancyFailed": 2,
  "factCheckPassed": 8,
  "factCheckFailed": 3,
  "results": [
    {
      "question": "What is the refund policy for purchases made within 30 days?",
      "expectedBehavior": "Should state that full refund is available within 30 days",
      "actualAnswer": "All purchases are eligible for a full refund within 30 days...",
      "relevancyPassed": true,
      "factCheckPassed": true,
      "category": "CORRECT_ANSWER"
    }
  ]
}
```

## Eval Dimensions Explained

| Dimension | What It Tests | Evaluator Used |
|---|---|---|
| Correct Answer | Agent answers accurately from knowledge base | RelevancyEvaluator + FactCheckingEvaluator |
| Hallucination Trap | Agent fabricates info not in context | FactCheckingEvaluator |
| Out of Scope | Agent acknowledges it can't answer | RelevancyEvaluator |
| Relevance Miss | Agent answers a different question | RelevancyEvaluator |

## Key Insight

RelevancyEvaluator and FactCheckingEvaluator test different things:
- A **relevant but wrong** answer passes RelevancyEvaluator but fails FactCheckingEvaluator
- An **irrelevant but factual** answer fails RelevancyEvaluator but may pass FactCheckingEvaluator
- You need **both** for production-grade evals

## Project Structure

```
src/main/kotlin/com/srini/aievals/
├── AiEvalsDemoApplication.kt      # Main application
├── config/
│   └── AiConfig.kt                # VectorStore, ChatClient, knowledge base loader
├── controller/
│   └── EvalController.kt          # REST endpoints
├── data/
│   ├── DataClasses.kt             # EvalCase, EvalResult, EvalSummary
│   └── EvalTestCases.kt           # 11 predefined test cases
└── service/
    ├── EvalService.kt             # Runs RelevancyEvaluator + FactCheckingEvaluator
    └── FaqAgentService.kt         # FAQ agent with RAG
```

## Troubleshooting

**Build fails with Kotlin version mismatch:**
Ensure you're using Kotlin 2.1.0+ (Spring AI 1.1.x requires Kotlin 2.x)

**FactCheckingEvaluator constructor error:**
If the single-arg constructor doesn't exist in your version, replace:
```kotlin
FactCheckingEvaluator(ChatClient.builder(chatModel))
```
with the builder pattern or two-arg constructor (check your Spring AI version's Javadoc).

**Missing dependency for QuestionAnswerAdvisor:**
If `spring-ai-advisors-vector-store` doesn't resolve, try adding:
```kotlin
implementation("org.springframework.ai:spring-ai-client-chat")
```