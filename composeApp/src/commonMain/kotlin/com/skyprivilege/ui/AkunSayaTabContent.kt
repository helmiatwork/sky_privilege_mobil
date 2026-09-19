package com.skyprivilege.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skyprivilege.data.remote.dto.CashierProfileDto

@Composable
fun AkunSayaTabContent(
    profile: CashierProfileDto?,
    isLoading: Boolean = false,
    deviceId: String = "DEV-TABLET-001",
    onRefresh: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onOpenChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Header Title Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Akun Saya",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Manajemen profil & kredensial keamanan kasir",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFDCFCE7))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "AKTIF",
                        color = Color(0xFF15803D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Profile Identity Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Large Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF005BAC))
                                .border(2.dp, Color(0xFF93C5FD), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val initial = profile?.name?.trim()?.firstOrNull()?.uppercase() ?: "K"
                            Text(
                                text = initial,
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile?.name ?: "Kasir SkyPrivilege",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "NIK: ${profile?.employeeId ?: "CSH-001"}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Outlet: ${profile?.outletName ?: "Sky Lounge T3 CGK"}",
                                fontSize = 12.sp,
                                color = Color(0xFF0284C7),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF005BAC)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Role Badge Row
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Role Kasir:",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = (profile?.role ?: "cashier").uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF005BAC)
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Pengaturan Kredensial",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Action: Edit Profile Name
                    AccountActionRow(
                        icon = "✏️",
                        title = "Ubah Nama Profil",
                        subtitle = "Perbarui nama kasir yang aktif",
                        onClick = onOpenEditProfile
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action: Change Password / PIN
                    AccountActionRow(
                        icon = "🔒",
                        title = "Ganti Password / PIN",
                        subtitle = "Ubah PIN keamanan autentikasi kasir",
                        onClick = onOpenChangePassword
                    )
                }
            }
        }

        // Device Information Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Informasi Perangkat POS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Device Hardware ID", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(deviceId, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status Whitelist", fontSize = 12.sp, color = Color(0xFF64748B))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("ACTIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tipe Perangkat", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text("Kiosk Tablet Terminal 3", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                    }
                }
            }
        }

        // Logout Button
        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFE11D48)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3))
            ) {
                Text("🚪 Keluar / Logout", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun AccountActionRow(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE0F2FE)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Text("›", fontSize = 20.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
        }
    }
}
