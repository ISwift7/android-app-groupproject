package com.example.act22.data.model

data class Portfolio(
    val userID: Int,
    val techStocks: MutableList<TechStock>,
    val cryptos: MutableList<Crypto>
) {
    init {
        require(techStocks.size <= 10) { "techStocks can have at most 10 items" }
        require(cryptos.size <= 3) { "cryptos can have at most 3 items" }
    }
}
