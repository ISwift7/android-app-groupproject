package com.example.act22.data.repository

import com.example.act22.data.model.Asset
import com.example.act22.data.model.PriceAlert

interface PriceAlertRepository {

    /**
     * Retrieves all price alerts for a specific asset.
     *
     * @param asset The asset for which price alerts are to be fetched.
     * @return A list of [PriceAlert] objects associated with the given asset.
     *         Returns an empty list if no alerts exist for the asset.
     */
    suspend fun getPriceAlertsForAsset(asset: Asset): List<PriceAlert>

    /**
     * Adds a new price alert for a specific asset.
     *
     * @param priceAlert class that holds asset info, date and price of alert.
     *
     * This function creates a new alert in the system for the specified asset,
     * allowing notifications when the asset's price meets the specified condition.
     */
    suspend fun addPriceAlert(priceAlert: PriceAlert)

    /**
     * Deletes an existing price alert for a specific asset.
     *
     * @param priceAlert class that holds asset info, date and price of alert.
     *
     * This function removes a specific alert matching the provided criteria
     * (asset ID, price, and date) from the system.
     */
    suspend fun deletePriceAlert(priceAlert: PriceAlert)

    /**
     * Checks all price alerts for a specific asset and determines which ones
     * have met their conditions.
     *
     * @param asset The asset whose price is to be checked against its alerts.
     * @return A list of [PriceAlert] objects that have been triggered (i.e., the
     *         asset's current price meets or exceeds the alert's price threshold).
     *
     * This function can be used to notify users about price alerts that are now active.
     */
    suspend fun checkPriceAlerts(asset: Asset): List<PriceAlert>
}
