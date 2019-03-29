package utils

sealed trait AccountType

case object Debit extends AccountType
case object Credit extends AccountType
case object Deposit extends AccountType


