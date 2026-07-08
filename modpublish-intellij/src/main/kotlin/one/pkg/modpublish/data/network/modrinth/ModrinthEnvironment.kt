package one.pkg.modpublish.data.network.modrinth

import one.pkg.modpublish.util.resources.Lang.translate

enum class ModrinthEnvironment(val id: String, val categoryTranslationKey: String, val translationKey: String) {
    ClientOnly(
        "client_only",
        "environment.category.client_side_only",
        "environment.client_only"
    ), // Client-Side Only
    ClientAndServer(
        "client_and_server",
        "environment.category.client_and_server",
        "environment.client_and_server"
    ), // Client and server, Required on both
    ServerOnlyClientOptional(
        "server_only_client_optional",
        "environment.category.client_and_server",
        "environment.server_only_client_optional"
    ), // Client and server, optional on client
    ClientOnlyServerOptional(
        "client_only_server_optional",
        "environment.category.client_and_server",
        "environment.client_only_server_optional"
    ), // Client and server, optional on server
    ClientOrServer(
        "client_or_server",
        "environment.category.client_and_server",
        "environment.client_or_server"
    ), // Client and server, optional on both
    ClientOrServerPrefersBoth(
        "client_or_server_prefers_both",
        "environment.category.client_and_server",
        "environment.client_or_server_prefers_both"
    ), // Client and server, best when installed on both
    ServerOnly(
        "server_only",
        "environment.category.server_side_only",
        "environment.server_only"
    ), // Server-side only, works in singleplayer too
    DedicatedServerOnly(
        "dedicated_server_only",
        "environment.category.server_side_only",
        "environment.dedicated_server_only"
    ), // Server-side only
    SingleplayerOnly(
        "singleplayer_only",
        "environment.category.singleplayer_only",
        "environment.singleplayer_only"
    ); // Singleplayer only

    override fun toString(): String {
        return "[${categoryTranslationKey.translate()}] ${translationKey.translate()}"
    }
}
