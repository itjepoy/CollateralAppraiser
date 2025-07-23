package com.cremcashcamfin.collateralappraiser.helper

import android.util.Log
import com.cremcashcamfin.collateralappraiser.model.ClientInfo
import java.sql.Connection
import java.sql.DriverManager

object DBHelper {

    private const val DB_CREMCAS = "CREM_CAS"
    private const val DB_EXCLUSIVE = "ExclusiveDataInfoCREM"
    private const val DB_HVC = "CREMLIC_HVC"

    /**
     * Generic method to create a JDBC connection to a specific database.
     */
    private fun getConnection(dbName: String): Connection? {
        return try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val url = "jdbc:jtds:sqlserver://${DatabaseConfig.ip}:${DatabaseConfig.port}/$dbName"
            DriverManager.getConnection(url, DatabaseConfig.username, DatabaseConfig.password)
        } catch (e: Exception) {
            Log.e("DBHelper", "Connection failed to $dbName", e)
            null
        }
    }

    /**
     * Establishes a connection to the default CREM_CAS database.
     */
    fun connect(): Connection? {
        return getConnection(DB_CREMCAS)
    }

    /**
     * Establishes a connection to ExclusiveDataInfoCREM database.
     */
    fun dbConnect(): Connection? {
        return getConnection(DB_EXCLUSIVE)
    }

    /**
     * Retrieves a list of clients with StepStatus = 'Step4'.
     */
    fun getClientNames(): List<ClientInfo> {
        val clients = mutableListOf<ClientInfo>()
        val conn = getConnection(DB_EXCLUSIVE) ?: return clients

        try {
            conn.createStatement().use { stmt ->
                val query = "SELECT ID, ControlNo, Fullname FROM IndividualInformation WHERE StepStatus = 'Step4'"
                stmt.executeQuery(query).use { rs ->
                    while (rs.next()) {
                        val indID = rs.getString("ID") ?: ""
                        val controlNo = rs.getString("ControlNo") ?: ""
                        val fullname = rs.getString("Fullname") ?: ""
                        clients.add(ClientInfo(indID, controlNo, fullname))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DBHelper", "getClientNames failed", e)
        } finally {
            try {
                conn.close()
            } catch (e: Exception) {
                Log.e("DBHelper", "Failed to close connection", e)
            }
        }

        return clients
    }

    /**
     * Retrieves the Collateral Class value using the Control No.
     */
    fun getCollateralClass(controlNo: String): String? {
        var classValue: String? = null
        val conn = getConnection(DB_HVC) ?: return null

        try {
            val query = "SELECT Class FROM Collaterals WHERE ClientID = ?"
            conn.prepareStatement(query).use { stmt ->
                stmt.setString(1, controlNo)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        classValue = rs.getString("Class")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DBHelper", "getCollateralClass failed", e)
        } finally {
            try {
                conn.close()
            } catch (e: Exception) {
                Log.e("DBHelper", "Failed to close connection", e)
            }
        }

        return classValue
    }

    fun savePropertyCoordinate(indID: String, controlNo: String, empID: String, latitude: Double?, longitude: Double?): Boolean {
        return try {
            val conn = dbConnect() ?: return false

            val checkSql = """
                SELECT COUNT(*) FROM APRPropertyLocation WHERE IIID = ? AND ControlNo = ?
                """.trimIndent()
            val checkStmt = conn.prepareStatement(checkSql)
            checkStmt.setString(1, indID)
            checkStmt.setString(2, controlNo)
            val rs = checkStmt.executeQuery()

            var exists = false
            if (rs.next()) {
                exists = rs.getInt(1) > 0
            }
            rs.close()
            checkStmt.close()

            if (exists) {
                Log.d("DBHelper", "Record already exists for IIID=$indID and ControlNo=$controlNo")
                conn.close()
                return false
            }

            val sql = """
                INSERT INTO APRPropertyLocation (IIID, ControlNo, EmployeeID, Latitude, Longitude)
                VALUES (?, ?, ?, ?, ?)
            """.trimIndent()

            val stmt = conn.prepareStatement(sql)
            stmt.setString(1, indID)
            stmt.setString(2, controlNo)
            stmt.setString(3, empID)
            if (latitude != null)
                stmt.setDouble(4, latitude)
            else
                stmt.setNull(4, java.sql.Types.DOUBLE)

            if (longitude != null)
                stmt.setDouble(5, longitude)
            else
                stmt.setNull(5, java.sql.Types.DOUBLE)

            val rows = stmt.executeUpdate()
            stmt.close()
            conn.close()
            rows > 0

        } catch (e: Exception) {
            Log.e("DBHelper", "Error saving property coordinate", e)
            false
        }
    }

    /**
     * Saves a collateral photo record into the appropriate SQL Server table,
     * depending on the collateral class ("REM" or "CM").
     */

    fun saveCollateralPhoto(
        colClass: String,
        indID: String,
        controlNo: String,
        filename: String,
        title: String,
        description: String,
        ext: String,
        imageBytes: ByteArray,
        latitude: Double?,
        longitude: Double?
    ): Boolean {
        return try {
            val conn = dbConnect() ?: return false
            val stmt = when (colClass.uppercase()) {
                "REM" -> {
                    val sql = """
                    INSERT INTO APRBIRZonal (
                        IIID, ControlNo, Filename, Title, Description, Ext, ImageData, Latitude, Longitude
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()
                    conn.prepareStatement(sql).apply {
                        setString(1, indID)
                        setString(2, controlNo)
                        setString(3, filename)
                        setString(4, title)
                        setString(5, description)
                        setString(6, ext)
                        setBytes(7, imageBytes)
                        setObject(8, latitude ?: 0.0)
                        setObject(9, longitude ?: 0.0)
                    }
                }

                "CM" -> {
                    val sql = """
                    INSERT INTO APRCMAttachment (
                        IIID, ControlNo, Filename, Title, Description, Ext, ImageData
                    ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()
                    conn.prepareStatement(sql).apply {
                        setString(1, indID)
                        setString(2, controlNo)
                        setString(3, filename)
                        setString(4, title)
                        setString(5, description)
                        setString(6, ext)
                        setBytes(7, imageBytes)
                    }
                }

                else -> return false
            }

            val rows = stmt.executeUpdate()
            stmt.close()
            conn.close()
            rows > 0

        } catch (e: Exception) {
            Log.e("DBHelper", "Error saving collateral photo", e)
            false
        }
    }



}
