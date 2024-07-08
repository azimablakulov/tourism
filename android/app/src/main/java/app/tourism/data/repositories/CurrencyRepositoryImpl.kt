package app.tourism.data.repositories

import app.tourism.data.db.Database
import app.tourism.data.dto.currency.CurrenciesList
import app.tourism.data.remote.CurrencyApi
import app.tourism.data.remote.handleGenericCall
import app.tourism.domain.models.profile.CurrencyRates
import app.tourism.domain.models.resource.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.Double.Companion.NaN

class CurrencyRepository(private val api: CurrencyApi, private val db: Database) {
    val currenciesDao = db.currencyRatesDao

    suspend fun getCurrency(): Flow<Resource<CurrencyRates>> = flow {
        currenciesDao.getCurrencyRates()?.let {
            emit(Resource.Success(it.toCurrencyRates()))
        }

        handleGenericCall(
            call = { api.getCurrency() },
            mapper = {
                val currencyRates = getCurrencyRatesFromXml(it)
                db.currencyRatesDao.updateCurrencyRates(currencyRates.toCurrencyRatesEntity())
                currencyRates
            },
        )
    }

    private fun getCurrencyRatesFromXml(it: CurrenciesList): CurrencyRates {
        val currencies = it.currencies
        fun findValueByCurrencyCode(code: String): Double {
            return currencies.firstOrNull { it.charCode == code }?.value ?: NaN
        }

        val usd = findValueByCurrencyCode("USD")
        val eur = findValueByCurrencyCode("EUR")
        val rub = findValueByCurrencyCode("RUB")

        return CurrencyRates(usd, eur, rub)
    }
}