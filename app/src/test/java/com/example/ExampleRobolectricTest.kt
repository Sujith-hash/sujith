package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.PaymentMethod
import com.example.model.Role
import com.example.viewmodel.BlinkViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("BlinkMart", appName)
    }

    @Test
    fun `test initial products and cart functionality`() {
        val viewModel = BlinkViewModel()
        assertTrue(viewModel.products.value.isNotEmpty())

        val firstProduct = viewModel.products.value.first()
        viewModel.addToCart(firstProduct)
        assertEquals(1, viewModel.cart.value.size)
        assertEquals(1, viewModel.cart.value.first().quantity)

        viewModel.addToCart(firstProduct)
        assertEquals(2, viewModel.cart.value.first().quantity)

        viewModel.decrementCart(firstProduct)
        assertEquals(1, viewModel.cart.value.first().quantity)
    }

    @Test
    fun `test role switching and placing order`() {
        val viewModel = BlinkViewModel()
        viewModel.switchRole(Role.MERCHANT)
        assertEquals(Role.MERCHANT, viewModel.activeRole.value)

        viewModel.switchRole(Role.CUSTOMER)
        val product = viewModel.products.value[1]
        viewModel.addToCart(product)

        val newOrder = viewModel.placeOrder(
            paymentMethod = PaymentMethod.UPI,
            deliveryAddress = "Indiranagar, Bangalore",
            tip = 20.0
        )

        assertNotNull(newOrder)
        assertEquals(0, viewModel.cart.value.size)
        assertTrue(viewModel.orders.value.any { it.id == newOrder?.id })
    }
}
