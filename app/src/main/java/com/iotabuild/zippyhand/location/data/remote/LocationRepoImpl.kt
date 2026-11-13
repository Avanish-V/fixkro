package com.iotabuild.zippyhand.location.data.remote

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.iotabuild.zippyhand.core.model.ApiResponse
import com.iotabuild.zippyhand.location.domain.LocationRepository
import com.iotabuild.zippyhand.location.data.local.LocationDao
import com.iotabuild.zippyhand.location.data.local.LocationEntity
import com.iotabuild.zippyhand.location.data.local.toDomain
import com.iotabuild.zippyhand.location.domain.Location
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class LocationRepoImpl(
    private val context: Context,
    private val locationDao: LocationDao,
    private val httpClient: HttpClient
) : LocationRepository {

    override suspend fun isExist(): Boolean {
        val location = locationDao.getLocation().firstOrNull()
        if (location != null) {
            return true
        } else {
            return false
        }
    }


    override fun observeLocation(): Flow<Location?> {
        return locationDao.getLocation().map { entity ->
            entity?.toDomain()
        }
    }

    private data class AddressInfo(
        val cityName: String,
        val stateName: String,
        val pinCode: Long,
        val streetAddress: String
    )

    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    override suspend fun fetchCurrentLocation(): Result<Location> {
        return try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val androidLocation = client.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await() ?: throw Exception("Unable to retrieve location from GPS")

            val addressInfo = getAddressFromLocation(
                context = context,
                latitude = androidLocation.latitude,
                longitude = androidLocation.longitude
            )

            val isServiceable = if (addressInfo.pinCode > 0) {
                val result = checkAvailable(addressInfo.pinCode.toString())
                result.getOrNull()?.isServiceable ?: false
            } else {
                false
            }

            val locationEntity = LocationEntity(
                id = 1,
                city = addressInfo.cityName,
                pinCode = addressInfo.pinCode,
                latitude = androidLocation.latitude,
                longitude = androidLocation.longitude,
                isServiceable = isServiceable,
                state = addressInfo.stateName,
                streetAddress = addressInfo.streetAddress
            )
            locationDao.insertLocation(locationEntity)
            Result.success(locationEntity.toDomain())

        } catch (e: SecurityException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkAvailability(pinCode: String): Result<ServiceAvailabilityResponse> {
        try {

            val response = httpClient.get("service-availability/check"){
                parameter("pincode", pinCode)
            }
            return if (response.status.value == 200) {
                val data = response.body<ServiceAvailabilityResponse>()

                Result.success(data)
            } else {
                return Result.failure(Exception("Failed to check availability"))
            }

        }catch (e: Exception){
            return Result.failure(Exception("Failed to check availability"))
        }
    }

    private suspend fun getAddressFromLocation(
        context: Context,
        latitude: Double,
        longitude: Double
    ): AddressInfo = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<android.location.Address>) {
                            continuation.resume(addresses)
                        }

                        override fun onError(errorMessage: String?) {
                            continuation.resume(emptyList())
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()
            }

            val addr = addresses.firstOrNull() ?: run {
                try {
                    val inGeocoder = Geocoder(context, Locale.forLanguageTag("en-IN"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        suspendCancellableCoroutine { continuation ->
                            inGeocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                                override fun onGeocode(addresses: MutableList<android.location.Address>) {
                                    continuation.resume(addresses.firstOrNull())
                                }

                                override fun onError(errorMessage: String?) {
                                    continuation.resume(null)
                                }
                            })
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        inGeocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
                    }
                } catch (e: Exception) {
                    null
                }
            }

            if (addr != null) {
                val adminArea = addr.adminArea ?: ""
                val locality = addr.locality ?: ""
                val subAdminArea = addr.subAdminArea ?: ""
                val subLocality = addr.subLocality ?: addr.featureName ?: ""
                val thoroughfare = addr.thoroughfare ?: ""
                val postalCodeStr = addr.postalCode ?: ""

                val cityName = when {
                    locality.isNotBlank() -> locality
                    subAdminArea.isNotBlank() -> subAdminArea
                    subLocality.isNotBlank() -> subLocality
                    else -> ""
                }
                val stateName = adminArea
                val pinCode = postalCodeStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
                val streetAddress = when {
                    subLocality.isNotBlank() && subLocality != cityName -> subLocality
                    thoroughfare.isNotBlank() -> thoroughfare
                    else -> ""
                }
                AddressInfo(
                    cityName = cityName,
                    stateName = stateName,
                    pinCode = pinCode,
                    streetAddress = streetAddress
                )
            } else {
                AddressInfo(
                    cityName = "Lat: %.3f, Lng: %.3f".format(latitude, longitude),
                    stateName = "",
                    pinCode = 0L,
                    streetAddress = ""
                )
            }
        } catch (e: Exception) {
            AddressInfo(
                cityName = "Lat: %.3f, Lng: %.3f".format(latitude, longitude),
                stateName = "",
                pinCode = 0L,
                streetAddress = ""
            )
        }
    }

    suspend fun checkAvailable(
        pinCode: String
    ): Result<ServiceAvailabilityResponse> {
        return try {
            val response = httpClient.get("service-availability/check") {
                parameter("pincode", pinCode)
            }.body<ApiResponse<ServiceAvailabilityResponse>>()

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Service unavailable")
                )
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
