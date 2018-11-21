package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class RequestFlow ( val requestName: String,
                    val rParty: Party) : FlowLogic<Unit>(){

    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
//        val criteria = QueryCriteria.VaultQueryCriteria()
//        val Vault = serviceHub.vaultService.queryBy<UserState>(criteria).states.first()
//        val request = serviceHub.identityService.partiesFromName(requestName,false).singleOrNull()
//                ?: throw IllegalArgumentException("No exact match found for request $requestName")
        val requester = GetState(rParty,this.ourIdentity,requestName)

        val cmd = Command (GetContract.Commands.Request(), listOf(ourIdentity.owningKey))
        val txBuilder = TransactionBuilder(notary)
                //.addInputState(Vault)
                .addOutputState(requester, GetContract.Get_Contract_ID)
                .addCommand(cmd)

        txBuilder.verify(serviceHub)

        val partySigned = serviceHub.signInitialTransaction(txBuilder)

//        val participantsParties = PartyState.participants.map {
//            serviceHub.identityService.wellKnownPartyFromAnonymous(otherParty)!!
//        }
//        val flowSession = (participantsParties - ourIdentity).map {
//            initiateFlow(otherParty)
//        }
//        val fullySigned = subFlow(CollectSignaturesFlow(partySigned,flowSession))

        subFlow(FinalityFlow(partySigned))

    }
}

//@InitiatingFlow
//@StartableByRPC
//class ForwardFlow ( val crequestName: String) : FlowLogic<Unit>(){
//
//    override val progressTracker = ProgressTracker()
//
//    @Suspendable
//    override fun call() {
//
//        val notary = serviceHub.networkMapCache.notaryIdentities.first()
////        val criteria = QueryCriteria.VaultQueryCriteria()
////        val Vault = serviceHub.vaultService.queryBy<UserState>(criteria).states.first()
//        val request = serviceHub.identityService.partiesFromName(crequestName,false).singleOrNull()
//                ?: throw IllegalArgumentException("No exact match found for request $crequestName")
//        val requester = GetState(ourIdentity,request,crequestName)
////        val senderOutputState = PartyState(ourIdentity,Vault.state.data.Node)
////        val receiverOutputState = PartyState(otherParty,ourIdentity)
//        val user = serviceHub.vaultService.queryBy<UserState>().states
//        val userRef = user.find { stateAndRef -> stateAndRef.state.data.isVerified  }
//        val outputUser  = userRef.state.data
//
//        val cmd = Command (GetContract.Commands.Request(), ourIdentity.owningKey)
//        val txBuilder = TransactionBuilder(notary)
//                //.addInputState(Vault)
//                //.addInputState(userRef)
//                .addOutputState(requester, GetContract.Get_Contract_ID)
//                .addCommand(cmd)
//
//        txBuilder.verify(serviceHub)
//
//        val partySigned = serviceHub.signInitialTransaction(txBuilder)
//        //val otherPartyFlow = initiateFlow(otherParty)
//
////        val participantsParties = PartyState.participants.map {
////            serviceHub.identityService.wellKnownPartyFromAnonymous(otherParty)!!
////        }
////        val flowSession = (participantsParties - ourIdentity).map {
////            initiateFlow(otherParty)
////        }
////        val fullySigned = subFlow(CollectSignaturesFlow(partySigned,flowSession))
//
//        //val signedTx = subFlow(CollectSignaturesFlow(partySigned, setOf(otherPartyFlow)))
//        subFlow(FinalityFlow(partySigned))
//
//    }
//}