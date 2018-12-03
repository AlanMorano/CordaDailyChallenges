package com.template

import com.template.flow.RequestFlow
import com.template.flow.UserRegisterFlow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class RequestFlowTest {

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
    fun `NoInputsShouldBeConsumed`(){
        val flow = UserRegisterFlow.Initiator("A",5,"ABC","A2","S","C")
        NodeA.startFlow(flow)
        val flow2 = RequestFlow.Initiator(NodeA.info.legalIdentities[0].name.organisation,"A")
        val future = NodeA.startFlow(flow2)
        network.runNetwork()
        val signedTransaction = future.get()
        assertEquals(0,signedTransaction.tx.inputs.size)

    }

    @Test
    @Throws
    fun `OneOutputShouldBeCreated`(){
        val flow = UserRegisterFlow.Initiator("A",5,"ABC","A2","S","C")
        NodeA.startFlow(flow)
        val flow2 = RequestFlow.Initiator(NodeA.info.legalIdentities[0].name.organisation,"A")
        val future = NodeA.startFlow(flow2)
        network.runNetwork()
        val signedTransaction = future.get()
        assertEquals(1,signedTransaction.tx.outputs.size)

    }

}