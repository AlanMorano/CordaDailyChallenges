package com.template
import com.template.states.KYCState
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.testing.core.TestIdentity
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class KYCStateTest {
    val testA = TestIdentity((CordaX500Name("Alice","New York","GB")))
    val testB = TestIdentity((CordaX500Name("Bob","Manila","PH")))



    @Test
    fun `NodeVariableIsTypeParty`() {
        var a = KYCState::class.java.getDeclaredField("node")
        assertEquals(Party::class.java, a.type)
    }

    @Test
    fun `NameVariableIsTypeString`() {
    var a = KYCState::class.java.getDeclaredField("name")
    assertEquals(String::class.java, a.type)
    }

    @Test
    fun `AgeVariableIsTypeInt`() {
        var a = KYCState::class.java.getDeclaredField("age")
        assertEquals(Int::class.java, a.type)
    }

    @Test
    fun `AddressVariableIsTypeString`() {
        var a = KYCState::class.java.getDeclaredField("address")
        assertEquals(String::class.java, a.type)

    }

    @Test
    fun `BirthDateVariableIsTypeDate`() {
        var a = KYCState::class.java.getDeclaredField("birthDate")
        assertEquals(String::class.java, a.type)
    }

    @Test
    fun `StatusVariableIsTypeString`() {
        var a = KYCState::class.java.getDeclaredField("status")
        assertEquals(String::class.java, a.type)
    }

    @Test
    fun `ReligionVariableIsTypeString`() {
        var a = KYCState::class.java.getDeclaredField("religion")
        assertEquals(String::class.java, a.type)
    }

    @Test
    fun `LinearIdIsUniqueIdentifier`(){
        var a = KYCState::class.java.getDeclaredField("linearId")
        assertEquals(UniqueIdentifier::class.java, a.type)
    }

    @Test
    fun `NodeIsParticipant`(){
        val uState = KYCState(testA.party,"Lance",20,"PH","July 1, 1998","Single","Catholic",true, listOf(testA.party,testB.party))
        assertNotEquals(uState.participants.indexOf(testA.party),-1)
    }

    @Test
    fun `IsContractState`(){
        assert(ContractState::class.java.isAssignableFrom(KYCState::class.java))
    }

    @Test
    fun `IsLinearState`(){
        assert(LinearState::class.java.isAssignableFrom(KYCState::class.java))
    }

    @Test
    fun `CheckTokenStateParameterOrdering`(){
        val fields = KYCState::class.java.declaredFields
        val nodeIndex = fields.indexOf(KYCState::class.java.getDeclaredField("node"))
        val nameIndex = fields.indexOf(KYCState::class.java.getDeclaredField("name"))
        val ageIndex = fields.indexOf(KYCState::class.java.getDeclaredField("age"))
        val addressIndex = fields.indexOf(KYCState::class.java.getDeclaredField("address"))
        val birthDateIndex = fields.indexOf(KYCState::class.java.getDeclaredField("birthDate"))
        val statusIndex = fields.indexOf(KYCState::class.java.getDeclaredField("status"))
        val religionIndex = fields.indexOf(KYCState::class.java.getDeclaredField("religion"))
        val isVerifiedIndex = fields.indexOf(KYCState::class.java.getDeclaredField("isVerified"))
        val partiesIndex = fields.indexOf(KYCState::class.java.getDeclaredField("listOfParties"))

        assert(nodeIndex<nameIndex)
        assert(nameIndex<ageIndex)
        assert(ageIndex<addressIndex)
        assert(addressIndex<birthDateIndex)
        assert(birthDateIndex<statusIndex)
        assert(statusIndex<religionIndex)
        assert(religionIndex<isVerifiedIndex)
        assert(religionIndex<partiesIndex)
    }


}