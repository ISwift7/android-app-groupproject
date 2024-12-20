package com.example.act22.data.repository

import kotlinx.coroutines.delay

class AiRepositoryTestImp :AiRepository{
        override suspend fun fetchAssetAnalysis(assetId: String): String {
            delay(2000)
            return "Asset $assetId shows strong growth potential in the next quarter."
        }

        override suspend fun fetchAssetPricePrediction(assetId: String): List<String> {
            delay(4000)
            return listOf(
                "Asset $assetId will go UP!",
                "Volatility expected to decrease in the next few days."
            )
        }

        override suspend fun fetchPortfolioRecommendation(): List<String> {
            delay(4000)
            return listOf(
                "Increase allocation to technology stocks.",
                "Reduce exposure to high-volatility cryptocurrencies.",
                "Consider adding green energy assets for diversification."
            )
        }
}