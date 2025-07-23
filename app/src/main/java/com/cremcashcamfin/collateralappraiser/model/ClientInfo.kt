package com.cremcashcamfin.collateralappraiser.model

/**
 * Data class representing basic client information.
 *
 * @property indID Unique identifier for the individual (e.g., primary key or external ID).
 * @property controlNo A control or reference number related to the client's transaction or record.
 * @property fullname Full name of the client.
 */
data class ClientInfo(
    val indID: String,
    val controlNo: String,
    val fullname: String
)