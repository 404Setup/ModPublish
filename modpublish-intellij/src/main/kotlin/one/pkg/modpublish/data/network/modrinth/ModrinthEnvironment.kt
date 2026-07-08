package one.pkg.modpublish.data.network.modrinth

import one.pkg.modpublish.util.resources.Lang

enum class ModrinthEnvironment(val id: String, val translationKey: String) {
    ClientAndServer("client_and_server", "environment.client_and_server"),
    ClientOnly("client_only", "environment.client_only"),
    ClientOnlyServerOptional("client_only_server_optional", "environment.client_only_server_optional"),
    SingleplayerOnly("singleplayer_only", "environment.singleplayer_only"),
    ServerOnly("server_only", "environment.server_only"),
    ServerOnlyClientOptional("server_only_client_optional", "environment.server_only_client_optional"),
    DedicatedServerOnly("dedicated_server_only", "environment.dedicated_server_only"),
    ClientOrServer("client_or_server", "environment.client_or_server"),
    ClientOrServerPrefersBoth("client_or_server_prefers_both", "environment.client_or_server_prefers_both");

    override fun toString(): String {
        return Lang.get(translationKey)
    }
}
