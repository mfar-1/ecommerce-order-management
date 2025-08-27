# 🛒 E-commerce Order Management System

Bu loyiha **Java Spring Boot** yordamida yaratilgan va mahsulotlar bilan buyurtmalarni boshqarish uchun xizmat qiladi.  
Unda **Swagger UI** orqali API’larni test qilish, hamda **JUnit 5** yordamida avtomatlashtirilgan testlar mavjud.  

---

## ✨ Asosiy imkoniyatlari
- 📦 Mahsulot qo‘shish, yangilash va o‘chirish
- 🛍️ Buyurtma yaratish va buyurtma holatini boshqarish
- 📊 Ombor zaxiralarini tekshirish
- ⚡ Xatoliklarni yagona handler orqali qaytarish
- ✅ Swagger UI orqali API’larni sinash
- 🧪 JUnit 5 testlari orqali kod sifatini tekshirish

---

## 🛠 Texnologiyalar
- ☕ Java 17+
- 🌱 Spring Boot
- 🗄 Spring Data JPA
- 🛢 H2 / MySQL
- 🧰 Maven
- 📖 Swagger UI
- 🧪 JUnit 5

---

## 🚀 Ishga tushirish
Loyihani ishga tushirish uchun quyidagilarni bajaring:

```bash
# loyihani klonlash
git clone https://github.com/mfar-1/ecommerce-order-management.git

# papkaga o‘tish
cd ecommerce-order-management

# loyihani ishlatish
mvn spring-boot:run

#Agar jarayon muvaffaqiyatli o‘tsa, loyiha http://localhost:8080
 manzilida ishga tushadi.

📖 API hujjatlar (Swagger)

Swagger UI orqali barcha endpointlarni test qilishingiz mumkin:

👉 http://localhost:8080/swagger-ui.html

## 📖 API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [api-docs.json](./api-docs.json)
- Postman Collection: [postman_collection.json](./postman_collection.json)

## 🧪 Test Coverage Report

Loyihada testlar **JUnit 5** yordamida yozilgan va `JaCoCo` orqali qamrov darajasi hisoblangan.  

### 📊 Test natijalari:
- **Line Coverage:** ~85%  
- **Branch Coverage:** ~78%  
- **Class Coverage:** ~90%  

### 📝 Izoh:
Testlar yordamida asosiy biznes logika, servis qatlamlari va REST API endpointlari muvaffaqiyatli qamrab olingan.  
Bu esa loyiha barqarorligini va kod sifatini ta’minlaydi. ✅

#💗 Mualif
github: mfar-1
