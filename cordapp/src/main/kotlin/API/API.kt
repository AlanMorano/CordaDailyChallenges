package API

import com.template.UserState
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.internal.x500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.NodeInfo
import net.corda.core.utilities.loggerFor
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.slf4j.Logger
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/api")
class API(val rpcOps: CordaRPCOps) {
    private val me = rpcOps.nodeInfo().legalIdentities.first().name

    companion object {
        private val logger: Logger = loggerFor<API>()
    }

    fun X500Name.toDisplayString(): String = BCStyle.INSTANCE.toString(this)

    /** Helpers for filtering the network map cache. */
    private fun isNotary(nodeInfo: NodeInfo) = rpcOps.notaryIdentities().any { nodeInfo.isLegalIdentity(it) }
    private fun isMe(nodeInfo: NodeInfo) = nodeInfo.legalIdentities.first().name == me
    private fun isNetworkMap(nodeInfo: NodeInfo) = nodeInfo.legalIdentities.single().name.organisation == "Network Map Service"

    /**
     * Returns the node's name.
     */
    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoami() = mapOf("me" to me.toString())

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GET
    @Path("/peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPeers(): Map<String, List<String>> {
        return mapOf("peers" to rpcOps.networkMapSnapshot()
                .filter { isNotary(it).not() && isMe(it).not() && isNetworkMap(it).not() }
                .map { it.legalIdentities.first().name.x500Name.toDisplayString() })
    }

    /**
     * Task 1
     * Displays all User states that exist in the node's vault.
     * TODO: Return a list of IOUStates on ledger
     * Hint - Use [rpcOps] to query the vault all unconsumed [UserState]s
     */
    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    fun getIOUs(): List<StateAndRef<ContractState>> {
        // Filter by state type: Users.
        return rpcOps.vaultQueryBy<UserState>().states
    }
}