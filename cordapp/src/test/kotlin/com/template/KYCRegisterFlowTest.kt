package com.template

import com.template.contract.KYCContract
import com.template.flow.KYCRegisterFlow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class KYCRegisterFlowTest {

    private lateinit var network : MockNetwork
    private lateinit var NodeA : StartedMockNode
    private lateinit var NodeB : StartedMockNode


    @Before
    fun setup() {
        network = MockNetwork(listOf("com.template"))
        NodeA = network.createPartyNode(null)
        NodeB = network.createPartyNode(null)
    }
    @After
    fun tearDown(){
        network.stopNodes()
    }


    @Test
    @Throws(Exception::class)
    fun `NoInputsShouldBeConsumed`(){
        val flow = KYCRegisterFlow.Initiator("A",5,"ABC","A2","S","C")
        val future = NodeA.startFlow(flow)
        network.runNetwork()
        val signedTransaction = future.get()
        assertEquals(0,signedTransaction.tx.inputs.size)
    }

    @Test
    @Throws(Exception::class)
    fun `OneOutputShouldBeCreated`(){
        val flow = KYCRegisterFlow.Initiator("A",5,"ABC","A2","S","C")
        val future = NodeA.startFlow(flow)
        network.runNetwork()
        val signedTransaction = future.get()
        assertEquals(1,signedTransaction.tx.outputs.size)
    }

    @Test
    @Throws(Exception::class)
    fun `transactionConstructedByFlowUsesTheCorrectNotary`() {
        val flow = KYCRegisterFlow.Initiator("A",5,"ABC","A2","S","C")
        val future = NodeA.startFlow(flow)
        network.runNetwork()
        val signedTransaction = future.get()
        assertEquals(1, signedTransaction.tx.outputStates.size)
        val (_,_ , notary) = signedTransaction.tx.outputs[0]
        assertEquals(network.notaryNodes[0].info.legalIdentities[0], notary)
    }

    @Test
    @Throws(Exception::class)
    fun `transactionConstructedByFlowHasOneIssueCommand`() {
        val flow = KYCRegisterFlow.Initiator("A",5,"ABC","A2","S","C")
        val future = NodeA.startFlow(flow)
        network.runNetwork()
        val signedTransaction = future.get()
        Assert.assertEquals(1, signedTransaction.tx.commands.size)
        val (value) = signedTransaction.tx.commands[0]
        assert(value is KYCContract.Commands.Register)
    }

    @Test
    @Throws(Exception::class)
    fun `RegisterFlowHasOneCommandOwnerIsSigner`() {
        val flow = KYCRegisterFlow.Initiator("A",5,"ABC","A2","S","C")
        val future = NodeA.startFlow(flow)
        network.runNetwork()
        val signedTransaction = future.get()
        Assert.assertEquals(1, signedTransaction.tx.commands.size)
        val (_, signers) = signedTransaction.tx.commands[0]
        Assert.assertEquals(1, signers.size.toLong())
        assert(signers.contains(NodeA.info.legalIdentities[0].owningKey))
    }


}