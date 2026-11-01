package com.gamenetmehdi.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamenetmehdi.app.ui.VenueColors

@Composable
fun AccountScreen(repository: AppRepository, navigator: AppNavigator) {
    val mode = navigator.accountMode
    var input by remember { mutableStateOf(AuthInput()) }
    var validation by remember { mutableStateOf(ValidationResult(emptyMap())) }
    var passwordVisible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    fun switchMode(next: AuthMode) {
        navigator.accountMode = next
        input = AuthInput()
        validation = ValidationResult(emptyMap())
        message = null
        passwordVisible = false
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(VenueColors.Slate950),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { BrandHeader("حساب کاربری") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                SectionHeading("ورود و ثبت‌نام", "فرم نمایشی بدون اتصال به سرور")
                SampleNotice("اطلاعات فرم ذخیره یا ثبت نمی‌شود و هیچ نشست واقعی ایجاد نخواهد شد.")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AuthMode.entries.forEach { item ->
                    FilterChip(selected = mode == item, onClick = { switchMode(item) }, label = { Text(item.label) })
                }
            }
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth().panel(22.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (mode == AuthMode.REGISTER) {
                    OutlinedTextField(
                        value = input.name,
                        onValueChange = { input = input.copy(name = it); message = null },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("نام و نام خانوادگی") },
                        isError = "name" in validation.errors,
                        supportingText = validation.errors["name"]?.let { error -> ({ Text(error) }) },
                    )
                }
                OutlinedTextField(
                    value = input.email,
                    onValueChange = { input = input.copy(email = it); message = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    label = { Text("ایمیل") },
                    isError = "email" in validation.errors,
                    supportingText = validation.errors["email"]?.let { error -> ({ Text(error) }) },
                )
                OutlinedTextField(
                    value = input.password,
                    onValueChange = { input = input.copy(password = it); message = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(if (passwordVisible) "پنهان" else "نمایش", fontSize = 10.sp)
                        }
                    },
                    label = { Text("رمز عبور") },
                    isError = "password" in validation.errors,
                    supportingText = validation.errors["password"]?.let { error -> ({ Text(error) }) },
                )
                if (mode == AuthMode.REGISTER) {
                    OutlinedTextField(
                        value = input.passwordConfirmation,
                        onValueChange = { input = input.copy(passwordConfirmation = it); message = null },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        label = { Text("تکرار رمز عبور") },
                        isError = "passwordConfirmation" in validation.errors,
                        supportingText = validation.errors["passwordConfirmation"]?.let { error -> ({ Text(error) }) },
                    )
                }
                PrimaryAction(if (mode == AuthMode.LOGIN) "بررسی ورود نمونه" else "بررسی ثبت‌نام نمونه") {
                    validation = repository.validate(mode, input)
                    val result = repository.submitDemo(mode, input)
                    message = result.message
                    if (result.accepted) {
                        input = AuthInput()
                        passwordVisible = false
                    }
                }
                if (message != null) {
                    Text(
                        text = message.orEmpty(),
                        color = if (validation.isValid) VenueColors.Emerald300 else VenueColors.Red300,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        item {
            Text(
                "با ادامه دادن در این نسخه، هیچ داده‌ای به اینترنت ارسال نمی‌شود. در نسخه API، ارتباط فقط از مسیر HTTPS و با مدیریت امن نشست انجام خواهد شد.",
                color = VenueColors.Slate400,
                fontSize = 12.sp,
                lineHeight = 20.sp,
            )
        }
    }
}
