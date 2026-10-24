package com.example.data

import com.example.model.CartItem
import com.example.model.Category
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.model.Product

object SampleData {

    val categories = listOf(
        Category("all", "All Items", "✨", true),
        Category("veggies", "Veggies & Fruits", "🥦", true),
        Category("dairy", "Dairy & Bread", "🥛", true),
        Category("munchies", "Snacks & Munchies", "🍿", true),
        Category("instant", "Instant & Ready", "🍜", true),
        Category("drinks", "Cold Drinks", "🧃", false),
        Category("home", "Household & Care", "🧼", false)
    )

    val products = listOf(
        // Veggies & Fruits
        Product(
            id = "veg-1",
            name = "Fresh Hybrid Tomatoes",
            categoryId = "veggies",
            unit = "1 kg",
            originalPrice = 45.0,
            discountedPrice = 32.0,
            inStock = true,
            rating = 4.8,
            reviewsCount = 420,
            isVeg = true,
            emoji = "🍅",
            description = "Farm fresh juicy tomatoes picked daily from local organic farms."
        ),
        Product(
            id = "veg-2",
            name = "Baby Spinach (Palak)",
            categoryId = "veggies",
            unit = "250 g bunch",
            originalPrice = 30.0,
            discountedPrice = 22.0,
            inStock = true,
            rating = 4.9,
            reviewsCount = 310,
            isVeg = true,
            emoji = "🥬",
            description = "Tender, clean baby spinach leaves washed and vacuum chilled."
        ),
        Product(
            id = "veg-3",
            name = "Fresh Red Onions (Pyaz)",
            categoryId = "veggies",
            unit = "1 kg bag",
            originalPrice = 40.0,
            discountedPrice = 29.0,
            inStock = true,
            rating = 4.7,
            reviewsCount = 590,
            isVeg = true,
            emoji = "🧅",
            description = "Crisp, pungent red onions, staple for Indian culinary delicacies."
        ),
        Product(
            id = "veg-4",
            name = "Shimla Royal Apples",
            categoryId = "veggies",
            unit = "4 pcs (~600 g)",
            originalPrice = 160.0,
            discountedPrice = 129.0,
            inStock = true,
            rating = 4.9,
            reviewsCount = 280,
            isVeg = true,
            emoji = "🍎",
            description = "Sweet and crunchy Himachali apples with smooth red blush."
        ),
        Product(
            id = "veg-5",
            name = "Fresh Coriander & Mint Bunch",
            categoryId = "veggies",
            unit = "100 g each",
            originalPrice = 25.0,
            discountedPrice = 15.0,
            inStock = true,
            rating = 4.8,
            reviewsCount = 145,
            isVeg = true,
            emoji = "🌿",
            description = "Aromatic fresh herb combo for garnishing curries and making chutneys."
        ),

        // Dairy & Bread
        Product(
            id = "dairy-1",
            name = "Amul Taaza Fresh Toned Milk",
            categoryId = "dairy",
            unit = "500 ml pouch",
            originalPrice = 28.0,
            discountedPrice = 27.0,
            inStock = true,
            rating = 4.9,
            reviewsCount = 1420,
            isVeg = true,
            emoji = "🥛",
            description = "Pasteurized toned milk with 3.0% fat, rich in calcium and protein."
        ),
        Product(
            id = "dairy-2",
            name = "Amul Salted Table Butter",
            categoryId = "dairy",
            unit = "100 g pack",
            originalPrice = 60.0,
            discountedPrice = 56.0,
            inStock = true,
            rating = 4.9,
            reviewsCount = 890,
            isVeg = true,
            emoji = "🧈",
            description = "Utterly butterly delicious creamy salted butter loved across India."
        ),
        Product(
            id = "dairy-3",
            name = "Modern 100% Whole Wheat Bread",
            categoryId = "dairy",
            unit = "400 g pack",
            originalPrice = 55.0,
            discountedPrice = 48.0,
            inStock = true,
            rating = 4.7,
            reviewsCount = 410,
            isVeg = true,
            emoji = "🍞",
            description = "Soft, wholesome whole wheat bread baked fresh daily."
        ),
        Product(
            id = "dairy-4",
            name = "Mother Dairy Fresh Malai Paneer",
            categoryId = "dairy",
            unit = "200 g block",
            originalPrice = 95.0,
            discountedPrice = 85.0,
            inStock = true,
            rating = 4.8,
            reviewsCount = 630,
            isVeg = true,
            emoji = "🧀",
            description = "Soft and melt-in-the-mouth cottage cheese rich in natural protein."
        ),
        Product(
            id = "dairy-5",
            name = "Farm Fresh Brown Eggs (Pack of 6)",
            categoryId = "dairy",
            unit = "6 pcs",
            originalPrice = 75.0,
            discountedPrice = 62.0,
            inStock = true,
            rating = 4.8,
            reviewsCount = 512,
            isVeg = false,
            emoji = "🥚",
            description = "Grade A high-protein farm eggs with rich golden yolks."
        ),

        // Snacks & Munchies
        Product(
            id = "snack-1",
            name = "Lay's India's Magic Masala",
            categoryId = "munchies",
            unit = "73 g pouch",
            originalPrice = 30.0,
            discountedPrice = 28.0,
            inStock = true,
            rating = 4.9,
            reviewsCount = 1100,
            isVeg = true,
            emoji = "🥔",
            description = "Crunchy ridged potato chips seasoned with aromatic Indian spices."
        ),
        Product(
            id = "snack-2",
            name = "Kurkure Masala Munch Crisps",
            categoryId = "munchies",
            unit = "85 g pack",
            originalPrice = 25.0,
            discountedPrice = 22.0,
            inStock = true,
            rating = 4.8,
            reviewsCount = 760,
            isVeg = true,
            emoji = "🌶️",
            description = "Tedhe medhe spicy puffed corn crisps with unmistakable flavor."
        ),
        Product(
            id = "snack-3",
            name = "Haldiram's Nagpur Aloo Bhujia",
            categoryId = "munchies",
            unit = "200 g pack",
            originalPrice = 65.0,
            discountedPrice = 55.0,
            inStock = true,
            rating = 4.9,
            reviewsCount = 820,
            isVeg = true,
            emoji = "🥨",
            description = "Crispy gram flour and potato noodles spiked with tangy spices."
        ),
        Product(
            id = "snack-4",
            name = "Act II Golden Sizzle Butter Popcorn",
            categoryId = "munchies",
            unit = "150 g box",
            originalPrice = 50.0,
            discountedPrice = 42.0,
            inStock = true,
            rating = 4.7,
            reviewsCount = 340,
            isVeg = true,
            emoji = "🍿",
            description = "Warm movie-style popcorn ready in 3 minutes with real melted butter flavor."
        ),

        // Instant & Ready
        Product(
            id = "instant-1",
            name = "Maggi 2-Minute Masala Noodles",
            categoryId = "instant",
            unit = "4 x 70 g (280 g)",
            originalPrice = 56.0,
            discountedPrice = 52.0,
            inStock = true,
            rating = 4.9,
            reviewsCount = 2400,
            isVeg = true,
            emoji = "🍜",
            description = "India's favorite instant noodles infused with 10 roasted spices."
        ),
        Product(
            id = "instant-2",
            name = "Knorr Classic Hot & Sour Veg Soup",
            categoryId = "instant",
            unit = "43 g pack",
            originalPrice = 40.0,
            discountedPrice = 35.0,
            inStock = true,
            rating = 4.6,
            reviewsCount = 210,
            isVeg = true,
            emoji = "🥣",
            description = "Tangy restaurant-style soup with real crunchy vegetables."
        ),
        Product(
            id = "instant-3",
            name = "Tata Sampann Protein Moong Chilla Mix",
            categoryId = "instant",
            unit = "180 g",
            originalPrice = 80.0,
            discountedPrice = 68.0,
            inStock = true,
            rating = 4.7,
            reviewsCount = 180,
            isVeg = true,
            emoji = "🥞",
            description = "Nutritious savory pancake batter mix packed with unpolished dal."
        ),

        // Cold Drinks
        Product(
            id = "drink-1",
            name = "Coca-Cola Zero Sugar Can",
            categoryId = "drinks",
            unit = "300 ml can",
            originalPrice = 40.0,
            discountedPrice = 36.0,
            inStock = true,
            rating = 4.8,
            reviewsCount = 920,
            isVeg = true,
            emoji = "🥤",
            description = "Crisp, effervescent cola taste with zero sugar and zero calories."
        ),
        Product(
            id = "drink-2",
            name = "Real Fruit Power Alphonso Mango Juice",
            categoryId = "drinks",
            unit = "1 L carton",
            originalPrice = 125.0,
            discountedPrice = 105.0,
            inStock = true,
            rating = 4.7,
            reviewsCount = 430,
            isVeg = true,
            emoji = "🥭",
            description = "Thick, nectarous juice crafted from Ratnagiri Alphonso mangoes."
        ),
        Product(
            id = "drink-3",
            name = "Raw Pressery Tender Coconut Water",
            categoryId = "drinks",
            unit = "200 ml bottle",
            originalPrice = 60.0,
            discountedPrice = 52.0,
            inStock = true,
            rating = 4.9,
            reviewsCount = 370,
            isVeg = true,
            emoji = "🥥",
            description = "100% natural cold pressed coconut water with zero preservatives."
        ),

        // Household & Care
        Product(
            id = "home-1",
            name = "Dettol Original Germ Protection Handwash",
            categoryId = "home",
            unit = "200 ml pump bottle",
            originalPrice = 99.0,
            discountedPrice = 84.0,
            inStock = true,
            rating = 4.9,
            reviewsCount = 670,
            isVeg = true,
            emoji = "🧴",
            description = "Kills 99.9% germs with moisturizing pine aroma."
        ),
        Product(
            id = "home-2",
            name = "Kleenex Extra Soft 2-Ply Facial Tissues",
            categoryId = "home",
            unit = "100 pulls box",
            originalPrice = 85.0,
            discountedPrice = 72.0,
            inStock = true,
            rating = 4.8,
            reviewsCount = 280,
            isVeg = true,
            emoji = "🧻",
            description = "Ultra gentle dermatologically tested absorbent tissues."
        )
    )

    fun createInitialActiveOrder(): Order {
        val sampleItems = listOf(
            CartItem(products[0], 2), // Tomatoes 2kg
            CartItem(products[5], 2), // Amul Milk 2 pouches
            CartItem(products[10], 1) // Lay's Magic Masala
        )
        val itemTotal = (32.0 * 2) + (27.0 * 2) + 28.0 // 64 + 54 + 28 = 146
        return Order(
            id = "BM-9281",
            customerName = "Aarav Sharma",
            customerPhone = "+91 98765 43210",
            deliveryAddress = "Flat 402, Green Glen Heights, 12th Main, Indiranagar, Bengaluru",
            deliveryInstructions = "Leave at door, please don't ring doorbell",
            items = sampleItems,
            itemTotal = itemTotal,
            deliveryFee = 0.0, // Free delivery promo!
            handlingFee = 5.0,
            tip = 20.0,
            grandTotal = itemTotal + 5.0 + 20.0,
            paymentMethod = PaymentMethod.UPI,
            paymentConfirmed = true,
            status = OrderStatus.OUT_FOR_DELIVERY,
            placedAtMillis = System.currentTimeMillis() - (5 * 60 * 1000), // 5 mins ago
            estimatedMinutes = 7,
            riderName = "Rahul Kumar",
            riderPhone = "+91 99887 76655",
            riderVehicle = "Ather 450X (KA-03-EV-4122)",
            riderRating = 4.9,
            liveProgress = 0.58f,
            deliveryOtp = "4819"
        )
    }
}
