# OrderManager Bug Fix Plan

## Issues Identified

1. **Order card images not displaying** - Images show as placeholders instead of actual product images
2. **Order creation RLS error** - "new row violates row-level security policy for table 'pedido_items'" when creating new orders
3. **Order details not visible in history** - Cannot see product details for completed orders in HistorialScreen
4. **App crashes when marking order as sent** - Application closes when attempting to mark an order as completed/delivered

## Root Cause Analysis

### Issue 1: Order Card Images Not Displaying
**Location**:
- `OrderCard.kt` (lines 105-120) - Image loading logic
- `MenuImages.kt` (lines 7-27) - Image resource mapping
- `MenuViewModel.kt` (line 167) - getImageRes method
- `MenuSeleccionScreen.kt` (line 146) - Menu item image loading

**Problem**:
The image lookup uses different data sources in different places:
- In menu screen: Uses `item.imagenRef` (image reference field)
- In order card: Uses `product.nombre` (product name field)
- `MenuImages.getImageResId()` expects product names as keys, not image references

However, upon closer inspection, both should theoretically work if the data is consistent. The more likely issue is **case sensitivity mismatch**:
- `MenuImages.productImageMap` uses exact case matches (e.g., "Hamburguesa Clásica")
- If product names in database have different casing (e.g., "hamburguesa clásica"), lookup fails
- The `trim()` in `getImageResId()` handles whitespace but not case differences

### Issue 2: Order Creation RLS Error
**Location**:
- `SupabasePedidoRepository.kt` (lines 24-60) - createPedido method
- `MenuViewModel.kt` (lines 93-132) - enviarPedido method
- Database RLS policies in `supabase/migrations/20260723220000_initial_order_manager.sql`

**Problem**:
The RLS policy for `pedido_items_insert_auth` requires:
```
EXISTS (SELECT 1 FROM pedidos WHERE pedidos.id = pedido_id AND pedidos.mesero_id = auth.uid())
OR (role IN ('admin', 'supervisor'))
```

The error indicates that when inserting into `pedido_items`, neither condition is met:
- The newly created pedido's `mesero_id` doesn't match `auth.uid()`
- The user is not authenticated as admin/supervisor

This suggests the `meseroId` being passed from the UI layer doesn't match the currently authenticated user's ID in Supabase.

### Issue 3: Order Details Not Showing in History
**Location**:
- `HistorialScreen.kt` (lines 95-117) - Product detail parsing in HistoryCard
- `SupabasePedidoRepository.kt` (lines 104-136) - toPedidoEntity method

**Problem**:
The `productos` JSON field in `PedidoEntity` is either:
- Malformed/invalid JSON
- Empty/null
- Does not match the expected format when parsed as JSONArray

When `JSONArray(order.productos)` fails, the code returns an empty list (lines 114-116), resulting in no products displayed.

### Issue 4: App Crashes When Marking Order as Sent
**Location**:
- `OrderViewModel.kt` (lines 67-105) - startSendConfirmation method
- `SupabasePedidoRepository.kt` (lines 94-102) - marcarCompletado method

**Problem**:
Potential issues:
- Unhandled exception during the `marcarCompletado` operation
- Navigation state corruption when removing/adding orders during the countdown
- Coroutine lifecycle issues with the counting timer

## Solution Plan

### Fix 1: Order Card Images
**Approach**: Standardize image lookup to use consistent data source
- Option 1: Modify `MenuImages` to map by `imagenRef` instead of `nombre`
- Option 2: Ensure both menu and order views use the same identifier (preferably `nombre`)
- Option 3: Make the lookup case-insensitive

**Recommended**: Make `MenuImages.getImageResId()` case-insensitive to handle any casing variations in the data.

### Fix 2: Order Creation RLS Error
**Approach**: Ensure `meseroId` matches authenticated user ID
- Verify how `meseroId` is obtained in the authentication flow
- Ensure `MenuViewModel.meseroId` is set to the actual Supabase user ID (`auth.uid()`)
- Pass the correct user ID from login/auth state to the menu screen

### Fix 3: Order Details in History
**Approach**: Validate and fix productos JSON generation
- Check that `toPedidoEntity` in `SupabasePedidoRepository` generates valid JSON
- Ensure the JSON structure matches what `HistorialScreen` expects to parse
- Add better error handling/logging to diagnose JSON parsing issues

### Fix 4: App Crash on Order Submission
**Approach**: Improve error handling and state management
- Add proper try/catch around `marcarCompletado` call
- Ensure UI state updates are handled correctly even if operation fails
- Verify coroutine scope and job management in the countdown timer

## Files to Modify

1. `app/src/main/java/com/example/ordermanager/ui/menu/MenuImages.kt` - Fix image lookup
2. `app/src/main/java/com/example/ordermanager/ui/viewmodel/MenuViewModel.kt` - Fix user ID handling
3. `app/src/main/java/com/example/ordermanager/backend/data/repository/SupabasePedidoRepository.kt` - Fix JSON generation and RLS issues
4. `app/src/main/java/com/example/ordermanager/ui/screens/HistorialScreen.kt` - Improve error handling for JSON parsing
5. `app/src/main/java/com/example/ordermanager/ui/viewmodel/OrderViewModel.kt` - Improve error handling for status updates

## Implementation Approach

Since I'm in plan mode and cannot make code changes, I will:
1. Document the specific changes needed for each file
2. Provide code snippets showing the fixes
3. Outline the expected behavior after fixes

This plan will be implemented by the user or another agent after plan approval.