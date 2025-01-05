package com.example.act22.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class AnalyzeSingleRequest(val asset_symbol: String)
data class AnalyzeSingleResponse(
    val success: Boolean,
    val results: List<String>?
)

data class PortfolioReportRequest(val portfolioAssetsIDs: List<String>)
data class PortfolioReportResponse(val success: Boolean, val report: String?)

data class PortfolioRecomandationsRequest(val portfolioAssetsIDs: List<String>)
data class PortfolioRecomandationsResponse(val success: Boolean, val recomendations: List<String>?)

interface AIApi {
    @POST("analyze_single")
    suspend fun analyzeSingleAsset(
        @Body request: AnalyzeSingleRequest
    ): Response<AnalyzeSingleResponse>

    @POST("portfolio_report")
    suspend fun getPortfolioReport(
        @Body request: PortfolioReportRequest
    ): Response<PortfolioReportResponse>

    @POST("portfolio_analysis")
    suspend fun getPortfolioRecomendations(
        @Body request: PortfolioRecomandationsRequest
    ): Response<PortfolioRecomandationsResponse>

}

