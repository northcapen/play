package models

import org.apache.commons.lang.math.DoubleRange

/**
 * Created by anton on 22/11/15.
 */
class CustomerKotlin(public val name: String, public val iin: IIN) {

    fun name(): String {
        return name + DoubleRange(1) + ", born at Kotlin Island";
    }
}