package com.cremcashcamfin.collateralappraiser.helper

import android.util.Log
import com.cremcashcamfin.collateralappraiser.model.ClientInfo
import java.sql.Connection
import java.sql.DriverManager

object DBHelper {
    val ip = "10.10.0.113"
    val port = "1433"
    val username = "sa"
    val password = "g@t3k33p3R2024"

    fun connect(): Connection? {

        val dbName = "CREM_CAS"
        return try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val url = "jdbc:jtds:sqlserver://$ip:$port/$dbName"
            DriverManager.getConnection(url, username, password)
        } catch (e: Exception) {
            Log.e("DBConnection", "Database connection error", e)
            null
        }
    }

    fun getClientNames(): List<ClientInfo> {
        val dbName = "ExclusiveDataInfoCREM"
        val clients = mutableListOf<ClientInfo>()
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val conn = DriverManager.getConnection(
                "jdbc:jtds:sqlserver://$ip:$port/$dbName",
                username, password
            )
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery("SELECT ControlNo, Fullname FROM IndividualInformation WHERE StepStatus = 'Step4'")
            while (rs.next()) {
                val controlNo = rs.getString("ControlNo") ?: ""
                val fullname = rs.getString("Fullname") ?: ""
                clients.add(ClientInfo(controlNo, fullname))
            }
            rs.close()
            stmt.close()
            conn.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return clients
    }

    fun getCollateralClass(controlNo: String): String? {
        val dbName = "CREMLIC_HVC"
        var classValue: String? = null
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val conn = DriverManager.getConnection(
                "jdbc:jtds:sqlserver://$ip:$port/$dbName",
                username, password
            )
            val query = "SELECT Class FROM Collaterals WHERE ClientID = ?"
            val stmt = conn.prepareStatement(query)
            stmt.setString(1, controlNo)
            val rs = stmt.executeQuery()
            if(rs.next()) {
                classValue = rs.getString("Class")
            }
            rs.close()
            stmt.close()
            conn.close()
        } catch (e: Exception) {
            Log.e("DBHelper", "getCollateralClass failed", e)
        }
        return classValue
    }
}
