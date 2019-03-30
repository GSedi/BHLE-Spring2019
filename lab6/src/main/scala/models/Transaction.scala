package models

sealed trait Transaction

case class ReplenishAnAccountModel(value: Int) extends Transaction
case class WithdrawFromAccountModel(value: Int) extends Transaction