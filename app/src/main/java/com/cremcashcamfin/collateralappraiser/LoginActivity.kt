package com.cremcashcamfin.collateralappraiser

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

        val sharedPref = getSharedPreferences("app_session", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)
        val fullname = sharedPref.getString("fullname", null)

        if (isLoggedIn && fullname != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("FULLNAME", fullname)
            startActivity(intent)
            finish()
        } else {
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
}

@SuppressLint("UseKtx")
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
                        employeeId = it
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
                        if (employeeId.isNotBlank() && password.isNotBlank()) {
                            scope.launch {
                                try {
                                    val loginResult = withContext(Dispatchers.IO) {
                                        val connection = DBHelper.connect()
                                        if (connection != null) {
                                            val hashedPassword = HashUtils.md5(password)
                                            Log.d("LoginDebug", "EmployeeID: $employeeId | Hashed: $hashedPassword")

                                            val stmt = connection.prepareStatement(
                                                """
                                                    SELECT UA.Employee_ID, Emp.Fullname
                                                    FROM [CAS].[dbo].[UserAccounts] UA
                                                    LEFT JOIN Employees Emp ON UA.Employee_ID = Emp.EmployeeID
                                                    WHERE UA.Employee_ID = ? AND UA.Password = ?
                                                """.trimIndent()
                                            )
                                            stmt.setString(1, employeeId)
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
                                        val sharedPref = context.getSharedPreferences("app_session", Context.MODE_PRIVATE)
                                        sharedPref.edit()
                                            .putBoolean("is_logged_in", true)
                                            .putString("employee_id", employeeId)
                                            .putString("fullname", loginResult)
                                            .apply()

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
