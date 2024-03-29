package com.rew3

import scalaz._
import Scalaz._


/**
 * Created by shishir on 9/9/15.
 */
package object es {
  type DomainValidation[+α] = Validation[NonEmptyList[String], α]

  /**
   * Trait for validation errors
   */
  trait ValidationKey {
    def failNel = this.toString.failureNel
    def nel = NonEmptyList(this.toString)
    def failure = this.toString.failure
  }

  object CommonValidations {
    /**
     * Validates that a string is not null and non empty
     *
     * @param s String to be validated
     * @param err ValidationKey
     * @return Validation
     */
    def checkString(s: String, err: ValidationKey): Validation[String, String] =
      if (s == null || s.isEmpty) err.failure else s.success

    /**
     * Validates that a date is a non zero Long value.
     *
     * @param d Long to be validated
     * @param err ValidationKey
     * @return Validation
     */
    def checkDate(d: Long, err: ValidationKey): Validation[String, Long] =
      if (d <= 0) err.failure else d.success


    /**
     * Validates a value is boolean
     * @param a
     * @param err
     * @return
     */
    def checkBoolean(a: Boolean, err: ValidationKey): Validation[String, Boolean] =
      if (a != true || a != false) err.failure else a.success
  }

  sealed case class Address (
                              street: String,
                              city: String,
                              stateOrProvince: String,
                              zip: String,
                              country: String
                              )

  object Address {
    import CommonValidations._

    case object StreetRequired extends ValidationKey
    case object CityRequired extends ValidationKey
    case object StateOrProvinceRequired extends ValidationKey
    case object CountryRequired extends ValidationKey
    case object ZipRequired extends ValidationKey

    def validate(street: String, city: String, stateOrProvince: String, country: String, zip: String):
    ValidationNel[String, Address] =
      (checkString(street, StreetRequired).toValidationNel |@|
        checkString(city, CityRequired).toValidationNel |@|
        checkString(stateOrProvince, StateOrProvinceRequired).toValidationNel |@|
        checkString(country, CountryRequired).toValidationNel |@|
        checkString(zip, ZipRequired).toValidationNel) {
        Address(_, _, _, _, _)
      }
  }
}
