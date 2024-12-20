package com.example.act22.data.repository

import com.example.act22.data.model.Asset
import com.example.act22.data.model.PriceAlert

class PriceAlertRepositoryTestImp : PriceAlertRepository {
    override suspend fun getPriceAlertsForAsset(asset: Asset): List<PriceAlert> {
        TODO("Not yet implemented")
    }

    override suspend fun addPriceAlert(priceAlert: PriceAlert) {
        TODO("Not yet implemented")
    }

    override suspend fun deletePriceAlert(priceAlert: PriceAlert) {
        TODO("Not yet implemented")
    }

    override suspend fun checkPriceAlerts(asset: Asset): List<PriceAlert> {
        TODO("Not yet implemented")
    }
}