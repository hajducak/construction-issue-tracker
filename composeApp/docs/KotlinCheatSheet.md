// VARIABLES
val name = "John"              // let name = "John" (immutable)
var age = 30                   // var age = 30 (mutable)

// NULLABILITY
val name: String? = null       // var name: String? = nil
val length = name?.length      // name?.count
val safe = name ?: "Unknown"   // name ?? "Unknown"

// DATA CLASSES (like Swift structs)
data class User(
    val id: String,
    val name: String,
    val role: String
)

// COLLECTIONS
val list = listOf(1, 2, 3)     // let list = [1, 2, 3]
val map = mapOf("a" to 1)      // let map = ["a": 1]

// FUNCTIONS
fun greet(name: String): String {
    return "Hello, $name"      // "Hello, \(name)"
}

// ENUMS
enum class Status {
    OPEN, IN_PROGRESS, FIXED, VERIFIED
}
