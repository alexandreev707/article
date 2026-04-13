package com.cryptodrop.web.error

/** OxaPay merchant invoice (payment link) creation failed — show message to user, avoid generic 500 HTML. */
class OxapayInvoiceException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
