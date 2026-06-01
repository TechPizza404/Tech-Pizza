package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini API General DTOs ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

// --- TechX Specification DTOs ---

@JsonClass(generateAdapter = true)
data class PhoneDetailSpecs(
    val display: String = "",
    val processor: String = "",
    val ram: String = "",
    val storage: String = "",
    val battery: String = "",
    val cameraMain: String = "",
    val cameraSelfie: String = ""
)

@JsonClass(generateAdapter = true)
data class PhoneDetailReview(
    val pros: List<String> = emptyList(),
    val cons: List<String> = emptyList(),
    val summary: String = ""
)

@JsonClass(generateAdapter = true)
data class PhoneDetailResponse(
    val brand: String = "",
    val model: String = "",
    val releaseDate: String = "",
    val specs: PhoneDetailSpecs = PhoneDetailSpecs(),
    val review: PhoneDetailReview = PhoneDetailReview()
)

@JsonClass(generateAdapter = true)
data class ComparisonAdvantages(
    val phoneA_advantages: List<String> = emptyList(),
    val phoneB_advantages: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ComparisonTargetAudience(
    val buyPhoneAIf: String = "",
    val buyPhoneBIf: String = ""
)

@JsonClass(generateAdapter = true)
data class CompareResponse(
    val winner: String = "",
    val comparisonSummary: String = "",
    val advantages: ComparisonAdvantages = ComparisonAdvantages(),
    val targetAudience: ComparisonTargetAudience = ComparisonTargetAudience()
)

@JsonClass(generateAdapter = true)
data class SearchResponseItem(
    val model: String = "",
    val matchReason: String = "",
    val estimatedPrice: String = ""
)

// --- Retrofit API Service ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

// --- TechX Gemini Repository Client ---

object GeminiClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }

    private val systemInstruction = Content(
        parts = listOf(Part(text = """
            You are the AI backend for TechX, a minimalist, professional, and completely objective smartphone specification and review platform. Your role is to act as an expert mobile technology analyst and an exhaustive smartphone database.

            Tone & Style Constraints:
            - Maintain a professional, journalistic, and strictly unbiased tone.
            - Avoid marketing fluff, hype words (e.g., 'game-changer', 'revolutionary'), or overly enthusiastic language.
            - Be highly precise with technical metrics (e.g., mAh, refresh rates in Hz, camera megapixels, processor nanometer nodes).
            - When asked for JSON, return ONLY valid JSON without markdown formatting blocks or conversational filler.
        """.trimIndent()))
    )

    suspend fun getPhoneDetail(phoneModel: String): PhoneDetailResponse = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw Exception("API Key has not been configured in Secrets Panel.")
        }
        val prompt = """
            Retrieve the technical specifications and write a concise, professional review for the $phoneModel.

            Output the response strictly as a JSON object with the following schema:
            {
              "brand": "string",
              "model": "string",
              "releaseDate": "string",
              "specs": {
                "display": "string",
                "processor": "string",
                "ram": "string",
                "storage": "string",
                "battery": "string",
                "cameraMain": "string",
                "cameraSelfie": "string"
              },
              "review": {
                "pros": ["array of 3 strings"],
                "cons": ["array of 3 strings"],
                "summary": "A 3-4 sentence professional verdict on the device."
              }
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(responseMimeType = "application/json", temperature = 0.1f),
            systemInstruction = systemInstruction
        )

        val rawResponse = RetrofitClient.service.generateContent(apiKey, request)
        val textResponse = rawResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response from Gemini")

        Log.d("GeminiClient", "Phone Detail Raw: $textResponse")

        moshi.adapter(PhoneDetailResponse::class.java).fromJson(textResponse)
            ?: throw Exception("Failed to parse phone specs JSON")
    }

    suspend fun comparePhones(phoneA: String, phoneB: String): CompareResponse = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw Exception("API Key has not been configured in Secrets Panel.")
        }
        val prompt = """
            Compare the $phoneA and the $phoneB.

            Output the response strictly as a JSON object with the following schema:
            {
              "winner": "string (Model name or 'Tie')",
              "comparisonSummary": "A 4-sentence objective summary comparing their strengths.",
              "advantages": {
                "phoneA_advantages": ["array of strings"],
                "phoneB_advantages": ["array of strings"]
              },
              "targetAudience": {
                "buyPhoneAIf": "string explanation",
                "buyPhoneBIf": "string explanation"
              }
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(responseMimeType = "application/json", temperature = 0.1f),
            systemInstruction = systemInstruction
        )

        val rawResponse = RetrofitClient.service.generateContent(apiKey, request)
        val textResponse = rawResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response from Gemini")

        Log.d("GeminiClient", "Compare Raw: $textResponse")

        moshi.adapter(CompareResponse::class.java).fromJson(textResponse)
            ?: throw Exception("Failed to parse compare JSON")
    }

    suspend fun searchPhones(query: String): List<SearchResponseItem> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw Exception("API Key has not been configured in Secrets Panel.")
        }
        val prompt = """
            The user is searching for: '$query'.
            Based on your knowledge of smartphones, recommend the top 3 devices that best match this query.

            Output the response strictly as a JSON array of objects, where each object has:
            {
              "model": "string",
              "matchReason": "1 sentence string explaining why it fits the query",
              "estimatedPrice": "string"
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(responseMimeType = "application/json", temperature = 0.1f),
            systemInstruction = systemInstruction
        )

        val rawResponse = RetrofitClient.service.generateContent(apiKey, request)
        val textResponse = rawResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response from Gemini")

        Log.d("GeminiClient", "Search Raw: $textResponse")

        val type = com.squareup.moshi.Types.newParameterizedType(List::class.java, SearchResponseItem::class.java)
        moshi.adapter<List<SearchResponseItem>>(type).fromJson(textResponse)
            ?: throw Exception("Failed to parse search JSON list")
    }
}
