package com.example.callshield

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request phone permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG
            ),
            100
        )

        // Request overlay permission
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        setContent {
            CallShieldApp()
        }
    }
}

@Composable
fun CallShieldApp() {

    var protectionEnabled by remember {
        mutableStateOf(true)
    }

    // =========================
    // CALLSHIELD COLOURS
    // =========================

    val background = Color(0xFF080511)
    val cardColor = Color(0xFF151020)
    val purple = Color(0xFF7C3AED)
    val purpleLight = Color(0xFF9D6CFF)
    val textWhite = Color(0xFFF8F5FF)
    val textGrey = Color(0xFFA8A2B5)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 24.dp)
        ) {

            // =========================
            // LOGO + NAME
            // =========================

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Image(
                    painter = painterResource(
                        id = R.drawable.callshield_logo
                    ),
                    contentDescription = "CallShield Logo",
                    modifier = Modifier.size(72.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {

                    Text(
                        text = "CallShield",
                        color = textWhite,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "KNOW WHO’S CALLING",
                        color = purpleLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(35.dp))

            // =========================
            // PROTECTION
            // =========================

            Text(
                text = "Protection",
                color = textWhite,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        cardColor,
                        RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = if (protectionEnabled)
                            "You are protected"
                        else
                            "Protection is off",
                        color = textWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (protectionEnabled)
                            "CallShield is monitoring incoming calls"
                        else
                            "Turn protection on to stay safe",
                        color = textGrey,
                        fontSize = 13.sp
                    )
                }

                Switch(
                    checked = protectionEnabled,
                    onCheckedChange = {
                        protectionEnabled = it
                    }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // =========================
            // SECURITY STATUS
            // =========================

            Text(
                text = "Security status",
                color = textWhite,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        cardColor,
                        RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(55.dp)
                        .background(
                            purple.copy(alpha = 0.18f),
                            RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "✓",
                        color = purpleLight,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {

                    Text(
                        text = "System protected",
                        color = textWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "Incoming calls are being checked",
                        color = textGrey,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // =========================
            // HOW CALLSHIELD WORKS
            // =========================

            Text(
                text = "How CallShield works",
                color = textWhite,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFF211535),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(22.dp)
            ) {

                Text(
                    text = "🛡",
                    fontSize = 30.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Know who's calling",
                    color = textWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = "CallShield checks incoming numbers against reported scam data and can warn you before you answer.",
                    color = textGrey,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }
        }
    }
}
