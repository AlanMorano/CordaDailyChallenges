package com.template

import com.template.flow.RequestKYCFlow
import com.template.flow.SendKYCFlow
import com.template.flow.KYCRegisterFlow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SendKYCFlowTest {

    private lateinit var network: MockNetwork
    private lateinit var NodeA: StartedMockNode
    private lateinit var NodeB: StartedMockNode


    @Before
    fun setup() {
        network = MockNetwork(listOf("com.template"))
        NodeA = network.createPartyNode(null)
        NodeB = network.createPartyNode(null)
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    @Throws
    fun `TwoInputsShouldBeConsumed`(){
        val flow = KYCRegisterFlow.Initiator("A",5,"ABC","A2","S","C")
        NodeA.startFlow(flow)
        val flow2 = RequestKYCFlow.Initiator(NodeA.info.legalIdentities[0].name.organisation,"A")
        NodeA.startFlow(flow2)
        val flow3 = SendKYCFlow.Initiator(NodeA.info.legalIdentities[0],"A")
        val future = NodeA.startFlow(flow3)
        network.runNetwork()
        val signedTransaction = future.get()
        assertEquals(2,signedTransaction.tx.inputs.size)
    }

    @Test
    @Throws
    fun `TwoOutputsShouldBeCreated`(){
        val flow = KYCRegisterFlow.Initiator("A",5,"ABC","A2","S","C")
        NodeA.startFlow(flow)
        val flow2 = RequestKYCFlow.Initiator(NodeA.info.legalIdentities[0].name.organisation,"A")
        NodeA.startFlow(flow2)
        val flow3 = SendKYCFlow.Initiator(NodeA.info.legalIdentities[0],"A")
        val future = NodeA.startFlow(flow3)
        network.runNetwork()
        val signedTransaction = future.get()
        assertEquals(2,signedTransaction.tx.outputs.size)

    }



}