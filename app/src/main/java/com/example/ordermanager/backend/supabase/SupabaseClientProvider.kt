package com.example.ordermanager.backend.supabase

import com.example.ordermanager.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClientProvider {

    val client: SupabaseClient by lazy {
        val url = BuildConfig.SUPABASE_URL.trim()
        val anonKey = BuildConfig.SUPABASE_ANON_KEY.trim()
        require(url.isNotEmpty()) { "Configura SUPABASE_URL en local.properties o gradle.properties" }
        require(anonKey.isNotEmpty()) { "Configura SUPABASE_ANON_KEY en local.properties o gradle.properties" }

        createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = anonKey
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}
