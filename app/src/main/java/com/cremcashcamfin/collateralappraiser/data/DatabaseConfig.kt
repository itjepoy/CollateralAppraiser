import android.content.Context
import java.util.Properties


/**
 * Singleton object to hold and load database configuration properties.
 * The properties are loaded from the 'db_config.properties' file located in the assets folder.
 */

object DatabaseConfig {
    var ip: String = ""
    var port: String = ""
    var username: String = ""
    var password: String = ""

    fun load(context: Context) {
        try {
            val props = Properties()
            // Open the properties file from the assets folder and load its content
            context.assets.open("db_config.properties").use { input ->
                props.load(input)
            }
            // Assign property values or default to an empty string if not found
            ip = props.getProperty("db_ip") ?: ""
            port = props.getProperty("db_port") ?: ""
            username = props.getProperty("db_username") ?: ""
            password = props.getProperty("db_password") ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
