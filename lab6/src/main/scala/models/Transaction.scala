package models

sealed trait Transaction

case class ReplenishAnAccountModel(value: BigInt) extends Transaction
case class WithdrawFromAccountModel(value: BigInt) extends Transaction
case class BalanceTransfer(tAccountId: BigInt, value: BigInt) extends Transaction