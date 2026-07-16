package com.example.hangon.data.repository

import com.example.hangon.data.model.Permission

interface PermissionRepository {
    fun getHomePermissions(): List<Permission>
    fun getOnboardingPermissions(): List<Permission>
}

class InMemoryPermissionRepository : PermissionRepository {

    override fun getHomePermissions(): List<Permission> = listOf(
        Permission(
            id = "contacts",
            title = "Contact Access",
            description = "Mencocokkan nomor masuk dengan kontak Anda.",
            isRequired = false,
            isGranted = true
        ),
        Permission(
            id = "audio",
            title = "Call Audio Access",
            description = "Diperlukan untuk merekam audio selama panggilan.",
            isRequired = true,
            isGranted = true
        ),
        Permission(
            id = "overlay",
            title = "Display Over Other Apps",
            description = "Diperlukan untuk menampilkan overlay peringatan.",
            isRequired = true,
            isGranted = false
        )
    )

    override fun getOnboardingPermissions(): List<Permission> = listOf(
        Permission(
            id = "call_screening",
            title = "Call Screening",
            description = "Diperlukan untuk mendeteksi panggilan masuk dan nomor yang tidak dikenal.",
            isRequired = true,
            isGranted = false
        ),
        Permission(
            id = "audio",
            title = "Akses Audio Panggilan",
            description = "Diperlukan untuk mendengarkan dan menganalisis audio selama panggilan berlangsung.",
            isRequired = true,
            isGranted = false
        ),
        Permission(
            id = "overlay",
            title = "Tampil di Atas App Lain",
            description = "Diperlukan untuk menampilkan overlay peringatan selama panggilan berjalan.",
            isRequired = true,
            isGranted = false
        ),
        Permission(
            id = "contacts",
            title = "Akses Kontak",
            description = "Opsional. Digunakan untuk mencocokkan nomor masuk dengan daftar kontak Anda.",
            isRequired = false,
            isGranted = false
        )
    )
}
