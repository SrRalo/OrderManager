package com.example.ordermanager.backend.data.repository

import com.example.ordermanager.backend.data.model.MenuItemInsert
import com.example.ordermanager.backend.data.model.MenuItemRow
import com.example.ordermanager.backend.supabase.SupabaseClientProvider
import com.example.ordermanager.ui.menu.MenuItem
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class SupabaseMenuRepository {

    private val client = SupabaseClientProvider.client

    suspend fun getItemsDisponibles(): List<MenuItem> {
        val items = client.from("items_menu")
            .select(columns = Columns.ALL) {
                filter {
                    eq("activo", true)
                    eq("disponible", true)
                }
                order("id", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
            }
            .decodeList<MenuItemRow>()

        return items.map { it.toMenuItem() }
    }

    suspend fun getAllItems(): List<MenuItemRow> {
        return client.from("items_menu")
            .select(columns = Columns.ALL) {
                filter { eq("activo", true) }
                order("id", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
            }
            .decodeList()
    }

    suspend fun updateDisponibilidad(itemId: Int, disponible: Boolean) {
        client.from("items_menu").update(
            {
                set("disponible", disponible)
            }
        ) {
            filter { eq("id", itemId) }
        }
    }

    suspend fun createItem(
        nombre: String,
        descripcion: String,
        precio: Double,
        categoriaId: Int,
        imagenRef: String
    ) {
        client.from("items_menu").insert(
            MenuItemInsert(
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                categoriaId = categoriaId,
                imagenRef = imagenRef,
                disponible = true,
                activo = true
            )
        )
    }

    suspend fun updateItem(
        itemId: Int,
        nombre: String,
        descripcion: String,
        precio: Double,
        categoriaId: Int,
        imagenRef: String
    ) {
        client.from("items_menu").update(
            {
                set("nombre", nombre)
                set("descripcion", descripcion)
                set("precio", precio)
                set("categoria_id", categoriaId)
                set("imagen_ref", imagenRef)
            }
        ) {
            filter { eq("id", itemId) }
        }
    }

    suspend fun deleteItem(itemId: Int) {
        client.from("items_menu").update(
            {
                set("activo", false)
            }
        ) {
            filter { eq("id", itemId) }
        }
    }

    private fun MenuItemRow.toMenuItem(): MenuItem {
        return MenuItem(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            categoriaId = categoriaId,
            imagenRef = imagenRef,
            disponible = disponible,
            activo = activo
        )
    }
}
