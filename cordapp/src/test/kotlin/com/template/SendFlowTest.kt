package com.template

import com.template.flow.RequestFlow
import com.template.flow.UserRegisterFlow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SendFlowTest {

    private lateinit var network : MockNetwork
    private lateinit var PartyA : StartedMockNode
    private lateinit var PartyB : StartedMockNode


    @Before
    fun setup() {
        network = MockNetwork(listOf("com.template"))
        PartyA = network.createPartyNode(null)
        PartyB = network.createPartyNode(null)
    }
    @After
    fun tearDown(){
        network.stopNodes()
    }

    @Test
    fun `hello`(){
        val flow = UserRegisterFlow.Initiator("Lance", 9,"PH","JULY3","S","C")
        val flow2 = RequestFlow.Initiator("PartyA","Lance" )
        val flow3 = SendFlow.Initiator("PartyB","Lance")

        PartyA.startFlow(flow)
        PartyB.startFlow(flow2)
        val future = PartyA.startFlow(flow3)
        network.runNetwork()
        val signedTransaction = future.get()
        assertEquals(2,signedTransaction.tx.inputs.size)
        assertEquals(2,signedTransaction.tx.outputs.size)



    }

}