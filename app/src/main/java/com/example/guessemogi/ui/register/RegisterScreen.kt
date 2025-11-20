package com.example.guessemogi.ui.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.guessemogi.viewmodel.authviewmodel.AuthViewModel
import com.example.guessemogi.R // Asegúrate de tener importado tu R

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onGoToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val email = viewModel.email.value
    val password = viewModel.password.value
    val loading = viewModel.loading.value
    val error = viewModel.errorMessage.value

    var passwordVisible by remember { mutableStateOf(false) }

    BackgroundScreen(backgroundResId = R.drawable.cahvito) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Crear Cuenta",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = viewModel::onEmailChange,
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image =
                                if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description =
                                if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = description)
                            }
                        }
                    )

                    if (error != null) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { if (!loading) viewModel.register(onRegisterSuccess) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading
                    ) {
                        Text(if (loading) "Creando..." else "Registrarse")
                    }

                    Spacer(Modifier.height(16.dp))

                    TextButton(onClick = onGoToLogin) {
                        Text("¿Ya tienes cuenta? Inicia Sesión")
                    }
                }
            }
        }
    }
}

@Composable
fun BackgroundScreen(backgroundResId: Int, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundResId),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        content()
    }
}
