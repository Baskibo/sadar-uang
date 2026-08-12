package com.example.sadaruang

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import android.view.inputmethod.InputMethodManager

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    private var isLoginMode = true
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        database = AppDatabase.getDatabase(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id)) // 🔥 penting
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val tvTitle = findViewById<TextView>(R.id.tv_title_login)
        val btnActionMain = findViewById<MaterialButton>(R.id.btn_action_main)
        val tvToggle = findViewById<TextView>(R.id.tv_toggle_mode)
        val btnOffline = findViewById<MaterialButton>(R.id.btn_offline)
        val btnGoogle = findViewById<MaterialButton>(R.id.btn_google)
        val etEmail = findViewById<android.widget.EditText>(R.id.et_email)
        val etPassword = findViewById<android.widget.EditText>(R.id.et_password)

        // Logic toggle Masuk / Daftar
        tvToggle.setOnClickListener {
            isLoginMode = !isLoginMode
            if (isLoginMode) {
                tvTitle.text = "Masuk ke Sadar Uang"
                btnActionMain.text = "Masuk"
                tvToggle.text = "Belum punya akun? Daftar di sini"
            } else {
                tvTitle.text = "Buat Akun Baru"
                btnActionMain.text = "Daftar Akun"
                tvToggle.text = "Sudah punya akun? Masuk di sini"
            }
        }

        // Logic tombol Masuk/Daftar (Dummy untuk saat ini)
        btnActionMain.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi email & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {

                if (isLoginMode) {
                    // ================= LOGIN =================
                    val user = database.userDao().login(email, password)

                    if (user != null) {
                        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
                        prefs.edit()
                            .putBoolean("isLoggedIn", true)
                            .putBoolean("isOffline", false)
                            .putString("userEmail", user.email) // 🔥 simpan siapa yang login
                            .apply()

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Email / Password salah", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    // ================= REGISTER =================
                    // ================= REGISTER =================
                    val existingUser = database.userDao().getUserByEmail(email)

                    if (existingUser != null) {
                        Toast.makeText(this@LoginActivity, "Email sudah terdaftar!", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    database.userDao().insert(
                        User(email = email, password = password)
                    )

                    Toast.makeText(this@LoginActivity, "Akun berhasil dibuat", Toast.LENGTH_SHORT).show()

                    // balik ke mode login otomatis
                    isLoginMode = true
                    tvTitle.text = "Masuk ke Sadar Uang"
                    btnActionMain.text = "Masuk"
                    tvToggle.text = "Belum punya akun? Daftar di sini"
                }
            }
        }

        btnGoogle.setOnClickListener {

            googleSignInClient.signOut() // 🔥 paksa reset dulu

            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 1001)
        }

        // Logic Mode Offline
        btnOffline.setOnClickListener {
            // Simpan status bahwa user memilih offline mode
            val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("isOffline", true)
                .putBoolean("isLoggedIn", false)
                .remove("userEmail")
                .apply()

            // Pindah ke MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Tutup halaman login agar tidak bisa di-back
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus

            if (view is EditText) {
                val outRect = android.graphics.Rect()
                view.getGlobalVisibleRect(outRect)

                // Jika klik di luar EditText
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    view.clearFocus()

                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.result

                val email = account.email ?: "google_user"

                val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("isLoggedIn", true)
                    .putBoolean("isOffline", false)
                    .putString("userEmail", email)
                    .apply()

                startActivity(Intent(this, MainActivity::class.java))
                finish()

            } catch (e: Exception) {
                Toast.makeText(this, "Login Google gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}