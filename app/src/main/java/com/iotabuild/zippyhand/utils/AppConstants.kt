package com.iotabuild.zippyhand.utils

object AppConstants {
    // Current Environment
    private const val IS_PRODUCTION = true


    // API Base URL
    // Local: "http://10.0.2.2:8080/api/v1/"
    // Production: Your AWS Lambda/API Gateway URL
    val BASE_URL = if (IS_PRODUCTION) {
        "https://nsir4pvum5npq5h2cfojgepfrq0hlerg.lambda-url.ap-south-1.on.aws/api/v1/"
    } else {
        // Use 10.0.2.2 for Android Emulator to access localhost
        "http://192.168.1.12:8085/api/v1/"
    }
}
