package com.template

import com.template.flow.RequestFlow
import com.template.flow.SendFlow
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



}