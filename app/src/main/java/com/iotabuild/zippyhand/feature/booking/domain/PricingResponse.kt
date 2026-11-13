package com.iotabuild.zippyhand.feature.booking.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal

object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return BigDecimal(decoder.decodeString())
    }
}

@Serializable
data class PriceDetailResponse(
    @Serializable(with = BigDecimalSerializer::class)
    val basePrice: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val discount: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val taxAmount: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val totalPrice: BigDecimal,
    val appliedCoupon: String? = null
)

@Serializable
data class PricingResponse(
    val success: Boolean,
    val data: PriceDetailResponse,
    val error: PricingError? = null
)

@Serializable
data class PricingError(
    val code: String,
    val message: String
)
