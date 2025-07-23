package com.cremcashcamfin.collateralappraiser

import DatabaseConfig
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cremcashcamfin.collateralappraiser.helper.DBHelper
import com.cremcashcamfin.collateralappraiser.helper.HashUtils
import com.cremcashcamfin.collateralappraiser.ui.theme.CollateralAppraiserTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DatabaseConfig.load(this)

        if (SessionManager.isLoggedIn(this)) {
            val fullname = SessionManager.getFullname(this)
            val empID = SessionManager.getEmpID(this)
            if (!fullname.isNullOrEmpty()) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("FULLNAME", fullname)
                intent.putExtra("EmployeeID", empID)
                startActivity(intent)
                finish()
                return
            }
        }

        setContent {
            CollateralAppraiserTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen()
                }
            }
        }
    }
}

@Composable
fun LoginScreen() {
    var employeeId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginAttempted by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.home_image),
            contentDescription = "Login Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Login", style = MaterialTheme.typography.headlineSmall)

                OutlinedTextField(
                    value = employeeId,
                    onValueChange = {
                        employeeId = it.uppercase()
                        if (loginAttempted) loginAttempted = false
                    },
                    label = { Text("Employee ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = loginAttempted && employeeId.isBlank(),
                    supportingText = {
                        if (loginAttempted && employeeId.isBlank()) {
                            Text("Employee ID is required", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (loginAttempted) loginAttempted = false
                    },
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = loginAttempted && password.isBlank(),
                    supportingText = {
                        if (loginAttempted && password.isBlank()) {
                            Text("Password is required", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = icon, contentDescription = "Toggle Password Visibility")
                        }
                    }
                )

                Button(
                    onClick = {
                        loginAttempted = true
                        val trimmedId = employeeId.trim()
                        val trimmedPass = password.trim()

                        if (trimmedId.isNotBlank() && trimmedPass.isNotBlank()) {
                            scope.launch {
                                try {
                                    val loginResult = withContext(Dispatchers.IO) {
                                        val connection = DBHelper.connect()
                                        if (connection != null) {
                                            val hashedPassword = HashUtils.md5(trimmedPass)
                                            Log.d("LoginDebug", "EmployeeID: $trimmedId | Hashed: $hashedPassword")

                                            val stmt = connection.prepareStatement(
                                                """
                                                    SELECT UA.Employee_ID, Emp.Fullname
                                                    FROM UserAccounts UA
                                                    LEFT JOIN Employees Emp ON UA.Employee_ID = Emp.EmployeeID
                                                    WHERE UA.Employee_ID = ? AND UA.Password = ?
                                                """.trimIndent()
                                            )
                                            stmt.setString(1, trimmedId)
                                            stmt.setString(2, hashedPassword)

                                            val rs = stmt.executeQuery()
                                            val isValid = rs.next()
                                            val fullname = if (isValid) rs.getString("Fullname") else null

                                            rs.close()
                                            stmt.close()
                                            connection.close()

                                            if (isValid) fullname else null
                                        } else {
                                            throw Exception("Connection failed.")
                                        }
                                    }

                                    if (loginResult != null) {
                                        SessionManager.setLogin(context, trimmedId, loginResult)

                                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(context, MainActivity::class.java).apply {
                                            putExtra("FULLNAME", loginResult)
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                                    }

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(context, "Login error: ${e.message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    CollateralAppraiserTheme {
        LoginScreen()
    }
}
