package com.template.contracts

import com.template.states.CashState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class CashContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.CashContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.commands.requireSingleCommand<Commands.Transfer>()
        requireThat {
            // Generic constraints around the IOU transaction.
            // "The IOU's value must be non-negative." using (tx.inputsOfType<CashState>().single().transferor.amount.quantity > 0)
            "Only one output state should be created." using (tx.outputs.size == 1)
            val out = tx.outputsOfType<CashState>().single()
            "The Sender and the Receiver cannot be the same entity." using (out.transferor != out.transferee)
            "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))
        }

    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Transfer : Commands
    }
}