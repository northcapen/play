package models

/**
 * Created by anton on 22/11/15.
 */
class AccountKotlin {
    var number: String = "123"

    fun isIBAN(): Boolean {
        return false;
    }

    override fun toString(): String {
        return "I was opened at Kotlin island";
    }
}