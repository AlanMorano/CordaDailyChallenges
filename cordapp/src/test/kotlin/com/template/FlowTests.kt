package com.template

import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class FlowTests {
    private val network = MockNetwork(listOf("com.template"))
    private val a = network.createNode()
    private val b = network.createNode()


    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `Request Test`() {
        val signedTransactionFuture = a.startFlow(RequestFlow(b.info.singleIdentity()))
        network.runNetwork()
        assertTrue(signedTransactionFuture.isDone)
    }
}