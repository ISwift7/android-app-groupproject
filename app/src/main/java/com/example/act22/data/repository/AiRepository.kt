package com.example.act22.data.repository

interface AiRepository {

    /**
     * Fetches AI-generated analysis for a specific asset.
     *
     * @param assetId The unique identifier of the asset (e.g., stock ticker or crypto symbol).
     * @return String with asset analysis
     * Example: ["Asset XYZ shows potential growth in Q4", "High institutional interest observed"]
     */
    suspend fun fetchAssetAnalysis(assetId: String): String

    /**
     * Fetches AI-predicted future price behavior for a specific asset.
     *
     * @param assetId The unique identifier of the asset (e.g., stock ticker or crypto symbol).
     * @return 2 strings - 1 UP DOWN or STABLE 2- mode about prediction
     * Example: ["Expected price range: $100 - $120 in the next week", "Volatility predicted to decrease"]
     */
    suspend fun fetchAssetPricePrediction(assetId: String): List<String>

    /**
     * Fetches AI-generated portfolio recommendations.
     *
     * @return 2 or more strings, 1 - Prediction about portfolio price in future, other - more info, recommendations too buy/sell stocks
     * Example: ["Increase allocation to technology stocks", "Consider adding green energy assets"]
     */
    suspend fun fetchPortfolioRecommendation(): List<String>
}
