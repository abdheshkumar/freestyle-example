sealed trait FormValidatorNel {

  import cats.data._
  import cats.data.Validated._
  import cats.implicits._

  type ValidationResult[A] = ValidatedNel[DomainValidation, A]


  def validateForm(username: String, password: String, firstName: String, lastName: String, age: Int): ValidationResult[RegistrationData] = {
    (validateUserName(username),
      validatePassword(password),
      validateFirstName(firstName),
      validateLastName(lastName),
      validateAge(age)).mapN(RegistrationData)
  }

  private def validateUserName(userName: String): ValidationResult[String] =
    if (userName.matches("^[a-zA-Z0-9]+$")) userName.validNel else UsernameHasSpecialCharacters.invalidNel

  private def validatePassword(password: String): ValidationResult[String] =
    if (password.matches("(?=^.{10,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$")) password.validNel
    else PasswordDoesNotMeetCriteria.invalidNel

  private def validateFirstName(firstName: String): ValidationResult[String] =
    if (firstName.matches("^[a-zA-Z]+$")) firstName.validNel else FirstNameHasSpecialCharacters.invalidNel

  private def validateLastName(lastName: String): ValidationResult[String] =
    if (lastName.matches("^[a-zA-Z]+$")) lastName.validNel else LastNameHasSpecialCharacters.invalidNel

  private def validateAge(age: Int): ValidationResult[Int] =
    if (age >= 18 && age <= 75) age.validNel else AgeIsInvalid.invalidNel

}


object FormValidatorNel extends FormValidatorNel