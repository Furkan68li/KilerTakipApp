package com.furkan.kilertakipp

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

// --- EKRAN YÖNETİMİ ---
enum class Screen { LOGIN, REGISTER }

@Composable
fun AuthScreen() {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(Screen.REGISTER) }

    AnimatedContent(targetState = currentScreen, label = "auth_transition") { screen ->
        when (screen) {
            Screen.REGISTER -> RegisterContent(
                onNavigateToLogin = { currentScreen = Screen.LOGIN },
                onRegisterClick = { email, password ->
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                                currentScreen = Screen.LOGIN
                            } else {
                                Toast.makeText(context, "Hata: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            )
            Screen.LOGIN -> LoginContent(
                onNavigateToRegister = { currentScreen = Screen.REGISTER },
                onLoginClick = { email, password ->
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Giriş Başarılı!", Toast.LENGTH_SHORT).show()
                                // TODO: Ana sayfaya yönlendir
                            } else {
                                Toast.makeText(context, "Hata: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            )
        }
    }
}

// --- KAYIT OL EKRANI ---
@Composable
fun RegisterContent(
    onNavigateToLogin: () -> Unit,
    onRegisterClick: (String, String) -> Unit // Parametre eklendi
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hey there,", fontSize = 16.sp)
        Text("Create an Account", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(30.dp))

        CustomTextField(value = firstName, onValueChange = { firstName = it }, label = "First Name", icon = Icons.Default.Person)
        CustomTextField(value = lastName, onValueChange = { lastName = it }, label = "Last Name", icon = Icons.Default.Person)
        CustomTextField(value = email, onValueChange = { email = it }, label = "Email", icon = Icons.Default.Email, keyboardType = KeyboardType.Email)
        CustomTextField(value = password, onValueChange = { password = it }, label = "Password", icon = Icons.Default.Lock, isPassword = true)

        Spacer(modifier = Modifier.height(30.dp))

        GradientButton(text = "Register") {
            if (email.isNotEmpty() && password.isNotEmpty()) {
                onRegisterClick(email, password) // Firebase fonksiyonu tetikleniyor
            }
        }
        // ... (SocialDivider ve GoogleButton aynı kalıyor)
        TextButton(onClick = onNavigateToLogin) {
            Text(buildAnnotatedString {
                append("Already have an account? ")
                withStyle(style = SpanStyle(color = Color(0xFF92A3FD), fontWeight = FontWeight.Bold)) { append("Login") }
            })
        }
    }
}

// --- GİRİŞ YAP EKRANI ---
@Composable
fun LoginContent(
    onNavigateToRegister: () -> Unit,
    onLoginClick: (String, String) -> Unit // Parametre eklendi
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", fontSize = 16.sp)
        Text("Welcome Back", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(40.dp))

        CustomTextField(value = email, onValueChange = { email = it }, label = "Email", icon = Icons.Default.Email)
        CustomTextField(value = password, onValueChange = { password = it }, label = "Password", icon = Icons.Default.Lock, isPassword = true)

        Spacer(modifier = Modifier.height(40.dp))

        GradientButton(text = "Login") {
            if (email.isNotEmpty() && password.isNotEmpty()) {
                onLoginClick(email, password) // Firebase fonksiyonu tetikleniyor
            }
        }
        // ... (Geri kalan UI bileşenleri aynı kalıyor)
        TextButton(onClick = onNavigateToRegister) {
            Text(buildAnnotatedString {
                append("Don't have an account yet? ")
                withStyle(style = SpanStyle(color = Color(0xFF92A3FD), fontWeight = FontWeight.Bold)) { append("Register") }
            })
        }
    }
}

// --- YARDIMCI BİLEŞENLER (COMPONENTLER) ---
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Gray) }, // Etiket rengi daha soft
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFF92A3FD)) },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    // Doğru ikonlar: Icons.Filled.Visibility ve VisibilityOff
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        // RENK GÜNCELLEMELERİ BURADA
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,    // Tıklandığında beyaz
            unfocusedContainerColor = Color.White,  // Boştayken beyaz
            focusedBorderColor = Color(0xFF92A3FD), // Odaklandığında mavi kenarlık
            unfocusedBorderColor = Color(0xFFE0E0E0), // Boştayken açık gri kenarlık
            cursorColor = Color(0xFF92A3FD)
        )
    )
}

@Composable
fun GradientButton(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "")

    Button(
        modifier = Modifier.fillMaxWidth().height(60.dp).scale(scale),
        onClick = onClick,
        interactionSource = interactionSource,
        contentPadding = PaddingValues(),
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                brush = Brush.horizontalGradient(colors = listOf(Color(0xFF92A3FD), Color(0xFF9DCEFF)))
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SocialDivider() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.LightGray)
        Text(text = " or ", modifier = Modifier.padding(horizontal = 8.dp), color = Color.Gray, fontSize = 14.sp)
        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.LightGray)
    }
}

@Composable
fun GoogleButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "")

    OutlinedButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth().height(55.dp).scale(scale),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = R.drawable.google), contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Continue with Google", color = Color.Black, fontWeight = FontWeight.Medium)
        }
    }
}