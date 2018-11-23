package com.template

import com.template.UserContract.Companion.USER_CONTRACT_ID
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class ContractTests {
    private val PartyA = TestIdentity(CordaX500Name("PartyA", "Pampanga", "PH"))
    private val PartyB = TestIdentity(CordaX500Name("PartyB", "Manila", "PH"))
    private val ledgerServices = MockServices()

    /* Register Test */
    @Test
    fun `Buy Command Requires No Input`() {
        ledgerServices.ledger {
            transaction {
                input(USER_CONTRACT_ID, UserState(PartyA.party, "User", 19, "Manila", "December 17, 1998", "Single", "Roman Catholic", false, listOf(PartyA.party), UniqueIdentifier()))
                output(USER_CONTRACT_ID, UserState(PartyA.party, "User", 19, "Manila", "December 17, 1998", "Single", "Roman Catholic", false, listOf(PartyA.party), UniqueIdentifier()))
                command(PartyA.publicKey, UserContract.Commands.Register())
                `fails with`("Transaction must have no input.")
            }
        }
    }

    @Test
    fun `Buy Command Requires One Input`() {
        ledgerServices.ledger {
            transaction {
                output(USER_CONTRACT_ID, UserState(PartyA.party, "User1", 19, "Manila", "December 17, 1998", "Single", "Roman Catholic", false, listOf(PartyA.party), UniqueIdentifier()))
                output(USER_CONTRACT_ID, UserState(PartyA.party, "User2", 19, "Manila", "December 17, 1998", "Single", "Roman Catholic", false, listOf(PartyA.party), UniqueIdentifier()))
                command(PartyA.publicKey, UserContract.Commands.Register())
                `fails with`("Transaction must have one output.")
            }
        }
    }

    @Test
    fun `Register user must be unverified`() {
        ledgerServices.ledger {
            transaction {
                output(USER_CONTRACT_ID, UserState(PartyA.party, "User2", 19, "Manila", "December 17, 1998", "Single", "Roman Catholic", true, listOf(PartyA.party), UniqueIdentifier()))
                command(PartyA.publicKey, UserContract.Commands.Register())
                `fails with`("User must not be registered as verified.")
            }
        }
    }


}