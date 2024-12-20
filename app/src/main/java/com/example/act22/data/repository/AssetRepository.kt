package com.example.act22.data.repository

import com.example.act22.data.model.Asset
import com.example.act22.data.model.AssetType
import com.example.act22.data.model.SortingCriteria

/**
 * Interface for managing asset-related operations.
 * Designed to be implemented for interacting with data sources such as Firebase Firestore or local databases.
 */
interface AssetRepository {

    /**
     * Fetches the complete list of assets from the data source.
     *
     * @return A list of [Asset] objects representing all available assets.
     * @throws Exception if there is an issue fetching the data (e.g., network or database error).
     */
    suspend fun getAllAssets(): List<Asset>

    /**
     * Searches for assets that match the given search query.
     *
     * @param search The search string to match against asset names or other attributes.
     * @return A list of [Asset] objects matching the search query.
     *         Returns an empty list if no matches are found.
     * @throws Exception if there is an issue performing the search (e.g., invalid query or data access error).
     */
    suspend fun searchAssets(search: String): List<Asset>

    /**
     * Filters assets by their type (e.g., stocks or cryptocurrencies).
     *
     * @param type The [AssetType] to filter assets by.
     * @return A list of [Asset] objects of the specified type.
     *         Returns an empty list if no assets of the specified type are found.
     * @throws Exception if there is an issue filtering the data (e.g., invalid type or data access error).
     */
    suspend fun filterAssetsByType(type: AssetType): List<Asset>

    /**
     * Sorts the list of assets based on the given sorting criteria.
     *
     * @param criteria The [SortingCriteria] to sort assets by (e.g., price ascending, price descending, alphabetical).
     * @return A list of [Asset] objects sorted according to the given criteria.
     * @throws Exception if there is an issue performing the sorting (e.g., invalid criteria or data access error).
     */
    suspend fun sortAssets(criteria: SortingCriteria): List<Asset>

    /**
     * Looks for asset with passed ID
     *
     * @param id unique indetifier of Asset
     * @return Asset if found
     * @throws Exception if there is an issue performing the search (e.g., invalid criteria or data access error).
     */
    suspend fun findAsset(id: String): Asset

    /**
     * Looks for yearly history price points of asset with passed ID
     *
     * @param id unique identifier of Asset
     * @return List of historical prices asset had on a beginning/end of each month last year
     * @throws Exception if there is an issue performing the search (e.g., invalid criteria or data access error).
     */
    suspend fun getYearlyHistoryPricePoints(id: String) : List<Double>

    /**
     * Looks for history price points of asset with passed ID
     *
     * @param id unique identifier of Asset
     * @return List of historical prices asset had during last 24 hours
     * @throws Exception if there is an issue performing the search (e.g., invalid criteria or data access error).
     */
    suspend fun getDailyHistoryPricePoints(id: String) : List<Double>
}
