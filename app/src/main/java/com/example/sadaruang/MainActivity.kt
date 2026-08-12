package com.example.sadaruang

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar
import android.widget.EditText
import com.google.android.material.button.MaterialButton
import android.app.DatePickerDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.net.Uri
import java.text.NumberFormat
import java.util.Locale
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Switch
import android.widget.Toast
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.reader.ReadableWorkbook
import java.util.stream.Collectors


class MainActivity : AppCompatActivity() {

    private lateinit var repo: PengeluaranRepository
    private var selectedImageUri: android.net.Uri? = null
    private var wallpaperUri: Uri? = null
    private var buktiUri: Uri? = null
    private var isPickingForWallpaper = false
    private var wallpaperPreview: ImageView? = null
    private var buktiPreview: ImageView? = null
    private lateinit var database: AppDatabase
    private lateinit var adapterPengeluaran: AdapterPengeluaran
    private var currentMonthView = Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-12
    private var currentYearView = Calendar.getInstance().get(Calendar.YEAR)
    private lateinit var googleSignInClient: GoogleSignInClient
    private val firestore = FirebaseFirestore.getInstance()


    private val pickExcel =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                importExcel(uri)
            }
        }

    private val createExcelFile =
        registerForActivityResult(ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )) { uri ->
            if (uri != null) {
                exportExcelToUri(uri)
            }
        }

    private fun exportExcelToUri(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
                val email = prefs.getString("userEmail", "offline") ?: "offline"

                val list = database.pengeluaranDao().getAllOnce(email)

                val os = contentResolver.openOutputStream(uri)

                val wb = Workbook(os, "SadarUang", "1.0")
                val ws = wb.newWorksheet("Pengeluaran")

                ws.value(0, 0, "Keterangan")
                ws.value(0, 1, "Kategori")
                ws.value(0, 2, "Nominal")
                ws.value(0, 3, "Tanggal")

                list.forEachIndexed { i, item ->
                    ws.value(i + 1, 0, item.keterangan)
                    ws.value(i + 1, 1, item.kategori)
                    ws.value(i + 1, 2, item.nominal.toDouble())
                    ws.value(i + 1, 3, item.tanggal)
                }

                wb.finish()
                os?.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Export berhasil!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(intent)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Export gagal: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun importExcel(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val input = contentResolver.openInputStream(uri)
                    ?: throw Exception("File tidak bisa dibuka")

                val wb = ReadableWorkbook(input)
                val sheet = wb.firstSheet

                val rows = sheet.openStream()
                    .skip(1)
                    .collect(Collectors.toList())

                for (row in rows) {
                    try {
                        val ket = row.getCellText(0) ?: ""
                        val kat = row.getCellText(1) ?: ""

                        val nominal = try {
                            when (val cell = row.getCell(2)?.value) {
                                is Number -> cell.toLong()
                                else -> row.getCellText(2)
                                    ?.replace("[^0-9]".toRegex(), "")
                                    ?.toLongOrNull() ?: 0L
                            }
                        } catch (e: Exception) {
                            0L
                        }

                        val tanggal = row.getCellText(3) ?: "01/01/2000"

                        val parts = tanggal.split("/")
                        val bulan = parts.getOrNull(1)?.toIntOrNull() ?: 1
                        val tahun = parts.getOrNull(2)?.toIntOrNull() ?: 2000

                        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
                        val email = prefs.getString("userEmail", "offline") ?: "offline"

                        val data = Pengeluaran(
                            keterangan = ket,
                            kategori = kat,
                            nominal = nominal,
                            tanggal = tanggal,
                            bulan = bulan,
                            tahun = tahun,
                            bukti = null,
                            userEmail = email
                        )

                        database.pengeluaranDao().insert(data)

                    } catch (e: Exception) {
                    }
                }

                wb.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Import berhasil!", Toast.LENGTH_SHORT).show()
                    recreateObserver()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Import gagal: ${e.message}", Toast.LENGTH_LONG).show()

                }
            }
        }
    }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->

            if (uri != null) {

                // 🔥 WAJIB: simpan izin permanen
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                if (isPickingForWallpaper) {
                    wallpaperUri = uri
                    wallpaperPreview?.setImageURI(uri)

                    val prefs = getSharedPreferences("wallpaper", MODE_PRIVATE)
                    prefs.edit()
                        .putString("wall_type", "custom")
                        .putString("wall_uri", uri.toString())
                        .apply()

                } else {
                    buktiUri = uri
                    buktiPreview?.setImageURI(uri)
                    buktiPreview?.visibility = View.VISIBLE
                }
            }
        }

    private fun applyWallpaper() {
        val prefs = getSharedPreferences("wallpaper", MODE_PRIVATE)
        val type = prefs.getString("wall_type", "default")
        val uriString = prefs.getString("wall_uri", null)

        val wallpaperView = findViewById<ImageView>(R.id.wallpaper)

        when (type) {
            "default" -> wallpaperView.setImageResource(R.drawable.black_wallpaper)
            "white" -> wallpaperView.setImageResource(R.drawable.white_wallpaper)
            "custom" -> {
                if (uriString != null) {
                    Glide.with(this)
                        .load(Uri.parse(uriString))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .centerCrop()
                        .into(wallpaperView)
                }
            }
        }
        wallpaperView.alpha = 0f
        wallpaperView.animate().alpha(1f).setDuration(400).start()


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

    private fun updateUIFromList(list: List<Pengeluaran>) {


        val tvTotalBulan = findViewById<TextView>(R.id.nominal_bulan)
        val tvRataRata = findViewById<TextView>(R.id.rata_rata)
        val tvJmlTransaksiBln = findViewById<TextView>(R.id.jumlah_transaksi)

        val tvBudget = findViewById<TextView>(R.id.budget_awal)
        val tvSisa = findViewById<TextView>(R.id.sisa_budget)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvPersen = findViewById<TextView>(R.id.persen)

        val prefs = getSharedPreferences("setting", MODE_PRIVATE)
        val budget = prefs.getInt("budget", 0)

        val nf = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        fun f(n: Long) = nf.format(n).replace(",00", "").replace("Rp", "Rp ")

        val totalBulan = list.sumOf { it.nominal }
        val jmlBln = list.size
        val rata = if (jmlBln > 0) totalBulan / jmlBln else 0

        tvTotalBulan.text = f(totalBulan)
        tvRataRata.text = "Rata-rata: ${f(rata)}"
        tvJmlTransaksiBln.text = "Jumlah Transaksi: $jmlBln"

        tvBudget.text = "Budget: ${f(budget.toLong())}"
        tvSisa.text = "Sisa: ${f(budget - totalBulan)}"

        val progress = if (budget > 0) ((totalBulan.toFloat() / budget.toFloat()) * 100).toInt() else 0
        progressBar.progress = progress.coerceAtMost(100)

        val persenAman = progress.coerceAtMost(100)
        tvPersen.text = "$persenAman%"

        tvPersen.setTextColor(
            if (persenAman >= 80) Color.RED else Color.WHITE
        )

        progressBar.progressTintList =
            ColorStateList.valueOf(if (progress >= 80) Color.RED else Color.WHITE)

        val notifAktif = prefs.getBoolean("notif_budget", false)
        val lastNotif = prefs.getBoolean("notif_shown", false)

        if (progress >= 80 && notifAktif && !lastNotif) {
            NotificationHelper(this).showNotification(
                "⚠️ Budget Hampir Habis",
                "Pengeluaran kamu sudah mencapai $progress% dari budget!"
            )
            prefs.edit().putBoolean("notif_shown", true).apply()
        }

        if (progress < 80) {
            prefs.edit().putBoolean("notif_shown", false).apply()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = AppDatabase.getDatabase(this)

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build()
        repo = PengeluaranRepository( database.pengeluaranDao(), firestore )
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        val btnDayNight = findViewById<ImageButton>(R.id.daynight)

        val prefs = getSharedPreferences("theme", MODE_PRIVATE)
        val isNight = prefs.getBoolean("night_mode", false)

        if (isNight) {

            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )

            btnDayNight.setImageResource(R.drawable.day_icon)

        } else {

            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )

            btnDayNight.setImageResource(R.drawable.night_icon)
        }

        btnDayNight.setOnClickListener {

            // anim fade sebelum ganti theme
            window.decorView.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction {

                    val nightMode =
                        AppCompatDelegate.getDefaultNightMode() ==
                                AppCompatDelegate.MODE_NIGHT_YES

                    if (nightMode) {

                        AppCompatDelegate.setDefaultNightMode(
                            AppCompatDelegate.MODE_NIGHT_NO
                        )

                        prefs.edit()
                            .putBoolean("night_mode", false)
                            .apply()

                    } else {

                        AppCompatDelegate.setDefaultNightMode(
                            AppCompatDelegate.MODE_NIGHT_YES
                        )

                        prefs.edit()
                            .putBoolean("night_mode", true)
                            .apply()
                    }

                    // fade in lagi
                    window.decorView.postDelayed({
                        window.decorView.alpha = 0f
                        window.decorView.animate()
                            .alpha(1f)
                            .setDuration(250)
                            .start()
                    }, 100)

                }
                .start()
        }

        val rvPengeluaran = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_pengeluaran)
        adapterPengeluaran = AdapterPengeluaran { item ->
            lifecycleScope.launch {
                repo.delete(item)

                item.firestoreId?.let {
                    firestore.collection("pengeluaran")
                        .document(it)
                        .delete()
                }

            }
        }
        rvPengeluaran.adapter = adapterPengeluaran
        rvPengeluaran.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        val btnExcel = findViewById<MaterialButton>(R.id.excel_btn)
        val btnImportMain = findViewById<MaterialButton>(R.id.import_btn)

        btnExcel.setOnClickListener {
            createExcelFile.launch("pengeluaran.xlsx")
        }

        btnImportMain.setOnClickListener {
            pickExcel.launch(arrayOf(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ))
        }

        val tvSelectedDate = findViewById<TextView>(R.id.bar_bulan) // Pastikan ID di XML sesuai (April 2026)
        val tvTotalTahunLabel = findViewById<TextView>(R.id.total_tahun)
        val btnKiri = findViewById<ImageButton>(R.id.kiri)
        val btnKanan = findViewById<ImageButton>(R.id.kanan)

        updateDateDisplay(tvSelectedDate, tvTotalTahunLabel)

        btnKanan.setOnClickListener {
            if (currentMonthView == 12) {
                currentMonthView = 1
                currentYearView++
            } else {
                currentMonthView++
            }
            updateDateDisplay(tvSelectedDate, tvTotalTahunLabel)
            recreateObserver() // Fungsi untuk ambil data dari Room
        }

        btnKiri.setOnClickListener {
            if (currentMonthView == 1) {
                currentMonthView = 12
                currentYearView--
            } else {
                currentMonthView--
            }
            updateDateDisplay(tvSelectedDate, tvTotalTahunLabel)
            recreateObserver()
        }
        // --- TAMBAHAN BARU: CEK SESSION ---
        val sessionPrefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val isLoggedIn = sessionPrefs.getBoolean("isLoggedIn", false)
        val isOffline = sessionPrefs.getBoolean("isOffline", false)
        val userEmail = sessionPrefs.getString("userEmail", null)

        // Jika belum login DAN belum milih offline, lempar ke LoginActivity
        if ((!isLoggedIn || userEmail == null) && !isOffline) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Mengatur padding agar UI tidak tertutup status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, 0)
            insets
        }

        // 1. Inisialisasi tombol FAB
        val fabAdd = findViewById<FloatingActionButton>(R.id.fab_tambah)
        val btnAnalytics = findViewById<com.google.android.material.button.MaterialButton>(R.id.graph_btn)
        val layoutAnalytics = findViewById<LinearLayout>(R.id.layout_analytics)

        // 2. Aksi ketika tombol ditekan
        fabAdd.setOnClickListener {
            showAddExpenseDialog()
        }
        val btnSetting = findViewById<ImageButton>(R.id.setting)

        btnAnalytics.setOnClickListener {

            if (layoutAnalytics.visibility == View.GONE) {
                expand(layoutAnalytics)

                layoutAnalytics.postDelayed({
                    loadAnalytics()
                }, 250)
            } else {
                collapse(layoutAnalytics)
            }
        }

        btnSetting.setOnClickListener {
            showSettingDialog()
        }
        recreateObserver()
        applyWallpaper()
        syncFromFirestore()

    }

    private var firestoreListener: ListenerRegistration? = null

    private fun syncFromFirestore() {

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val email = prefs.getString("userEmail", "offline") ?: "offline"

        firestoreListener?.remove() // 🔥 biar ga double listener

        firestoreListener = firestore.collection("pengeluaran")
            .whereEqualTo("userEmail", email)
            .addSnapshotListener { snapshots, e ->

                if (e != null || snapshots == null) return@addSnapshotListener

                lifecycleScope.launch(Dispatchers.IO) {

                    for (doc in snapshots.documents) {

                        if (doc.metadata.hasPendingWrites()) continue

                        val firestoreId = doc.id

                        val data = Pengeluaran(
                            keterangan = doc.getString("keterangan") ?: "",
                            kategori = doc.getString("kategori") ?: "",
                            nominal = doc.getLong("nominal") ?: 0L,
                            tanggal = doc.getString("tanggal") ?: "",
                            bulan = doc.getLong("bulan")?.toInt() ?: 1,
                            tahun = doc.getLong("tahun")?.toInt() ?: 2000,
                            bukti = doc.getString("bukti"),
                            userEmail = doc.getString("userEmail") ?: "offline",
                            firestoreId = firestoreId
                        )

                        val existing = database.pengeluaranDao()
                            .getByFirestoreId(firestoreId)

                        if (existing == null) {
                            database.pengeluaranDao().insert(data)
                        } else {
                            database.pengeluaranDao().update(
                                data.copy(id = existing.id)
                            )
                        }
                    }
                }
            }
    }

    private var currentLiveData: LiveData<List<Pengeluaran>>? = null

    private fun recreateObserver() {

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val email = prefs.getString("userEmail", "offline") ?: "offline"

        currentLiveData?.removeObservers(this)

        currentLiveData = database.pengeluaranDao()
            .getByMonthYear(currentMonthView, currentYearView, email)

        currentLiveData?.observe(this) { list ->

            if (list.isNullOrEmpty()) {
                findViewById<View>(R.id.blurEmpty).visibility = View.VISIBLE
                findViewById<RecyclerView>(R.id.rv_pengeluaran).visibility = View.GONE
            } else {
                findViewById<View>(R.id.blurEmpty).visibility = View.GONE
                findViewById<RecyclerView>(R.id.rv_pengeluaran).visibility = View.VISIBLE
                adapterPengeluaran.setData(list)
            }
            updateUIFromList(list)
        }

// 🔥 OBSERVER TAHUN
        database.pengeluaranDao()
            .getByYear(currentYearView, email)
            .observe(this) { listTahun ->

                val tvTotalTahunValue = findViewById<TextView>(R.id.jumlahpertahun)
                val tvjmlTransaksiT = findViewById<TextView>(R.id.jumlah_transaksi_tahun)

                val totalTahun = listTahun.sumOf { it.nominal }
                val jmlTahun = listTahun.size

                val nf = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

                tvTotalTahunValue.text = nf.format(totalTahun)
                    .replace(",00", "")
                    .replace("Rp", "Rp ")

                tvjmlTransaksiT.text = jmlTahun.toString()
            }
    }

    private fun loadAnalytics() {
        val pieChart = findViewById<com.github.mikephil.charting.charts.PieChart>(R.id.pieChart)
        val barChart = findViewById<com.github.mikephil.charting.charts.BarChart>(R.id.barChart)
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val email = prefs.getString("userEmail", "offline") ?: "offline"

        database.pengeluaranDao()
            .getByMonthYear(currentMonthView, currentYearView, email)
            .observe(this) { list ->

                // =====================
                // 📊 PIE CHART (Kategori)
                // =====================
                val kategoriMap = mutableMapOf<String, Float>()

                list.forEach {
                    val current = kategoriMap[it.kategori] ?: 0f
                    kategoriMap[it.kategori] = current + it.nominal.toFloat()
                }

                val pieEntries = kategoriMap.map {
                    com.github.mikephil.charting.data.PieEntry(it.value, it.key)
                }

                val pieDataSet = com.github.mikephil.charting.data.PieDataSet(pieEntries, "")
                val colors = kategoriMap.keys.map { kategori ->
                    val hash = kategori.hashCode()

                    val r = (hash shr 16 and 0xFF)
                    val g = (hash shr 8 and 0xFF)
                    val b = (hash and 0xFF)

                    android.graphics.Color.rgb(
                        (r + 128) % 256,
                        (g + 128) % 256,
                        (b + 128) % 256
                    )
                }

                pieDataSet.colors = colors
                pieDataSet.valueTextColor = android.graphics.Color.WHITE
                pieDataSet.valueTextSize = 12f

                pieChart.data = com.github.mikephil.charting.data.PieData(pieDataSet)

                pieChart.setUsePercentValues(true)
                pieChart.isDrawHoleEnabled = true
                pieChart.holeRadius = 60f

                pieChart.setEntryLabelColor(android.graphics.Color.WHITE)
                pieChart.setCenterTextColor(android.graphics.Color.WHITE)

                pieChart.description.isEnabled = false
                pieChart.centerText = "Kategori"
                pieChart.animateY(1000)

                // 📊 BAR CHART (Harian)
                val dayMap = mutableMapOf<Int, Float>()

                list.forEach {
                    val day = it.tanggal.substring(0, 2).toInt()
                    val current = dayMap[day] ?: 0f
                    dayMap[day] = current + it.nominal.toFloat()
                }

                val barEntries = dayMap.map {
                    com.github.mikephil.charting.data.BarEntry(it.key.toFloat(), it.value)
                }

                val barDataSet = com.github.mikephil.charting.data.BarDataSet(barEntries, "Harian")
                barDataSet.color = android.graphics.Color.CYAN

                barChart.data = com.github.mikephil.charting.data.BarData(barDataSet)
                barChart.description.isEnabled = false
                barChart.animateY(1000)
            }
    }


    private fun updateDateDisplay(tvDate: TextView, tvYearLabel: TextView) {
        val namaBulan = arrayOf("", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember")

        tvDate.text = "${namaBulan[currentMonthView]} $currentYearView"
        tvYearLabel.text = "Total Tahun $currentYearView"
    }

    private fun showSettingDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.overlaysetting)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val btnClose = dialog.findViewById<ImageButton>(R.id.btn_close_setting)
        btnClose.setOnClickListener { dialog.dismiss() }

        val tabBudget = dialog.findViewById<Button>(R.id.tab_budget)
        val tabNotif = dialog.findViewById<Button>(R.id.tab_notif)
        val tabWall = dialog.findViewById<Button>(R.id.tab_wall)
        val tabAkun = dialog.findViewById<Button>(R.id.tab_akun)

        val container = dialog.findViewById<FrameLayout>(R.id.content_container)

        // Sesuaikan parameter selectTab
        fun selectTab(selected: Button, vararg others: Button) {
            selected.setBackgroundColor(Color.parseColor("#3B82F6"))
            selected.setTextColor(Color.WHITE)

            others.forEach {
                it.setBackgroundColor(Color.TRANSPARENT)
                it.setTextColor(Color.LTGRAY)
            }
        }
        // DEFAULT TAB
        selectTab(tabBudget, tabNotif, tabWall, tabAkun)

        val view = inflateLayout(container, R.layout.item_setting_budget)
        setupBudget(view, dialog)

        tabBudget.setOnClickListener {
            selectTab(tabBudget, tabNotif, tabWall, tabAkun)
            val view = inflateLayout(container, R.layout.item_setting_budget)
            setupBudget(view, dialog)
        }

        tabNotif.setOnClickListener {
            selectTab(tabNotif, tabBudget, tabWall, tabAkun)
            val view = inflateLayout(container, R.layout.item_setting_notif)
            setupNotif(view)
        }

        tabWall.setOnClickListener {
            selectTab(tabWall, tabBudget, tabNotif, tabAkun)
            val view = inflateLayout(container, R.layout.item_setting_wall)
            setupWall(view, dialog)
        }

        // TAMBAHKAN LISTENER UNTUK TAB AKUN
        tabAkun.setOnClickListener {
            selectTab(tabAkun, tabBudget, tabNotif, tabWall)
            val view = inflateLayout(container, R.layout.item_setting_akun)
            setupAkun(view)
        }

        dialog.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )

        dialog.show()
    }

    private fun inflateLayout(container: FrameLayout, layoutId: Int): View {
        val newView = layoutInflater.inflate(layoutId, container, false)

        val animIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)

        container.removeAllViews()
        container.addView(newView)
        newView.startAnimation(animIn)

        return newView //  WAJIB
    }

    private fun setupBudget(view: View, dialog: Dialog) {
        val prefs = getSharedPreferences("setting", MODE_PRIVATE)

        val input = view.findViewById<EditText>(R.id.input_budget)

        val switchNotif = view.findViewById<Switch>(R.id.switch_notif)

// load
        val isOn = prefs.getBoolean("notif_budget", false)
        switchNotif.isChecked = isOn

        switchNotif.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit()
                .putBoolean("notif_budget", isChecked)
                .apply()

            Toast.makeText(
                this,
                if (isChecked) "Notif budget aktif"
                else "Notif budget dimatikan",
                Toast.LENGTH_SHORT
            ).show()
        }

        // load data
        val saved = prefs.getInt("budget", 0)
        if (saved != 0) {
            val formatted = NumberFormat
                .getCurrencyInstance(Locale.forLanguageTag("id-ID"))
                .format(saved)
                .replace(",00", "")
                .replace("Rp", "Rp ")

            input.setText(formatted)
        }

        // tombol cepat
        view.findViewById<MaterialButton>(R.id.btn_1jt).setOnClickListener {
            input.setText("1000000")
        }
        view.findViewById<MaterialButton>(R.id.btn_2jt).setOnClickListener {
            input.setText("2000000")
        }
        view.findViewById<MaterialButton>(R.id.btn_3jt).setOnClickListener {
            input.setText("3000000")
        }
        view.findViewById<MaterialButton>(R.id.btn_5jt).setOnClickListener {
            input.setText("5000000")
        }
        view.findViewById<MaterialButton>(R.id.btn_10jt).setOnClickListener {
            input.setText("10000000")
        }

        val btnSave = view.findViewById<MaterialButton>(R.id.btn_simpan_budget)
        btnSave.setOnClickListener {
            val raw = input.text.toString()
            val clean = raw.replace("[Rp,. ]".toRegex(), "")
            val value = clean.toIntOrNull() ?: 0

            val prefsEdit = getSharedPreferences("setting", MODE_PRIVATE)
            prefsEdit.edit().putInt("budget", value).apply()

            recreateObserver() // Update dashboard utama

            Toast.makeText(this, "Budget disimpan", Toast.LENGTH_SHORT).show()

            dialog.dismiss() //  INI AKAN MENUTUP DIALOG SETTING
        }

        input.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    input.removeTextChangedListener(this)

                    // hapus semua selain angka
                    val clean = s.toString().replace("[Rp,. ]".toRegex(), "")

                    if (clean.isNotEmpty()) {
                        val parsed = clean.toLong()

                        val formatted = NumberFormat
                            .getCurrencyInstance(Locale("in", "ID"))
                            .format(parsed)
                            .replace(",00", "") // hilangkan desimal
                            .replace("Rp", "Rp ")

                        current = formatted
                        input.setText(formatted)
                        input.setSelection(formatted.length)
                    } else {
                        current = ""
                        input.setText("")
                    }

                    input.addTextChangedListener(this)
                }
            }
        })

    }

    private fun setupNotif(view: View) {
        val prefs = getSharedPreferences("setting", MODE_PRIVATE)

        val switchNotif = view.findViewById<Switch>(R.id.switch_notif_catat)

        val workRequest =
            PeriodicWorkRequestBuilder<WeeklyWorker>(7, TimeUnit.DAYS)
                .build()

        switchNotif.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notif", isChecked).apply()

            if (isChecked) {
                WorkManager.getInstance(this)
                    .enqueueUniquePeriodicWork(
                        "weekly_notif",
                        ExistingPeriodicWorkPolicy.UPDATE,
                        workRequest
                    )
            } else {
                WorkManager.getInstance(this)
                    .cancelUniqueWork("weekly_notif")
            }
        }

        // load status
        val isOn = prefs.getBoolean("notif", false)
        switchNotif.isChecked = isOn

        val btnDone = view.findViewById<MaterialButton>(R.id.btn_selesai_notif)
        btnDone.setOnClickListener {
            Toast.makeText(this, "Pengaturan notif disimpan", Toast.LENGTH_SHORT).show()
        }

    }

    private fun setupWall(view: View, dialog: Dialog) {

        val prefs = getSharedPreferences("wallpaper", MODE_PRIVATE)
        val editor = prefs.edit()

        val preview = view.findViewById<ImageView>(R.id.img_preview_wallpaper)

        val black = view.findViewById<ImageView>(R.id.preview_black_wall)
        val white = view.findViewById<ImageView>(R.id.preview_white_wall)

        val btnUpload = view.findViewById<Button>(R.id.btn_upload)
        val btnReset = view.findViewById<Button>(R.id.btn_reset)
        val btnDone = view.findViewById<MaterialButton>(R.id.btn_selesai_wall)

        //  LOAD PREVIEW AWAL
        val type = prefs.getString("wall_type", "default")
        val uri = prefs.getString("wall_uri", null)

        when (type) {
            "default" -> preview.setImageResource(R.drawable.black_wallpaper)
            "white" -> preview.setImageResource(R.drawable.white_wallpaper)
            "custom" -> {
                if (uri != null) {
                    preview.setImageURI(android.net.Uri.parse(uri))
                }
            }
        }

        //  PILIH BLACK
        black.setOnClickListener {
            preview.setImageResource(R.drawable.black_wallpaper)

            selectedImageUri = null //  WAJIB

            editor.putString("wall_type", "default")
            editor.remove("wall_uri")
            editor.apply()
        }
        //  PILIH WHITE
        white.setOnClickListener {
            preview.setImageResource(R.drawable.white_wallpaper)

            selectedImageUri = null //  WAJIB

            editor.putString("wall_type", "white")
            editor.remove("wall_uri")
            editor.apply()
        }

        // 📸 UPLOAD
        btnUpload.setOnClickListener {
            isPickingForWallpaper = true
            pickImage.launch(arrayOf("image/*"))
        }

        wallpaperPreview = preview

        //  RESET
        btnReset.setOnClickListener {
            preview.setImageResource(R.drawable.black_wallpaper)

            selectedImageUri = null //  WAJIB

            editor.putString("wall_type", "default")
            editor.remove("wall_uri")
            editor.apply()

            Toast.makeText(this, "Kembali ke default", Toast.LENGTH_SHORT).show()
        }

        //  DONE

        btnDone.setOnClickListener {

            val currentType = prefs.getString("wall_type", "default")

            // hanya simpan custom kalau memang upload
            if (wallpaperUri != null && currentType == "custom") {
                editor.putString("wall_uri", wallpaperUri.toString())
                editor.apply()
            }

            applyWallpaper()
            dialog.dismiss()
        }
    }

    private fun setupAkun(view: View) {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val email = prefs.getString("userEmail", "Tidak diketahui")
        val sessionPrefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val isLoggedIn = sessionPrefs.getBoolean("isLoggedIn", false)
        val tvNama = view.findViewById<TextView>(R.id.tv_nama)
        val tvEmail = view.findViewById<TextView>(R.id.tv_email)
        val imgProfile = view.findViewById<ImageView>(R.id.img_profile)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btn_logout)
        val btnLoginSusulan = view.findViewById<MaterialButton>(R.id.btn_login_susulan)
        val btnExport = view.findViewById<MaterialButton>(R.id.btn_export_excel)
        val btnImport = view.findViewById<MaterialButton>(R.id.btn_import_excel)

        if (account != null) {
            // LOGIN GOOGLE
            tvNama.text = account.displayName ?: "User Google"
            tvEmail.text = account.email ?: "-"

            Glide.with(this)
                .load(account.photoUrl)
                .placeholder(R.drawable.offline_user) // optional
                .into(imgProfile)

        } else {
            // LOGIN MANUAL / DATABASE
            tvNama.text = email?.substringBefore("@") ?: "User"
            tvEmail.text = email

            imgProfile.setImageResource(R.drawable.offline_user)
        }

        btnLogout.setOnClickListener {

            googleSignInClient.signOut().addOnCompleteListener {

                // 🔥 TAMBAHAN PENTING (BIAR MUNCUL PILIH AKUN)
                googleSignInClient.revokeAccess().addOnCompleteListener {

                    val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()

                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }

        if (isLoggedIn) {
            btnLogout.text = "Keluar / Ganti Akun"
            btnLogout.setBackgroundColor(Color.parseColor("#EF4444"))
            btnLogout.visibility = View.VISIBLE
            btnLoginSusulan.visibility = View.GONE
        } else {
            btnLoginSusulan.text = "Login / Hubungkan Akun"
            btnLoginSusulan.visibility = View.VISIBLE
            btnLogout.visibility = View.GONE
        }

        // Logic jika user mau login dari mode offline
        btnLoginSusulan.setOnClickListener {
            // Lempar kembali ke LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Logic Excel (UI-nya saja dulu)
        btnExport.setOnClickListener {
            createExcelFile.launch("pengeluaran.xlsx")
        }

        btnImport.setOnClickListener {
            pickExcel.launch(arrayOf(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ))
            }
    }

    private fun showAddExpenseDialog() {

        buktiUri = null

        selectedImageUri = null

        val dialog = Dialog(this) // Sekarang 'this' akan terbaca dengan benar
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.overlaytambah)

        // Membuat background dialog transparan agar sudut CardView terlihat melengkung
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Mengatur lebar dialog agar responsif
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Di dalam showAddExpenseDialog
        val btnSimpan = dialog.findViewById<MaterialButton>(R.id.btn_simpan_pengeluaran)
        val btnUpload = dialog.findViewById<LinearLayout>(R.id.btn_upload_bukti)
        val ivPreview = dialog.findViewById<ImageView>(R.id.iv_preview)

        buktiPreview = ivPreview
        buktiPreview?.setImageDrawable(null)
        buktiPreview?.visibility = View.GONE

        btnUpload.setOnClickListener {
            isPickingForWallpaper = false
            pickImage.launch(arrayOf("image/*"))
        }

        val etKet = dialog.findViewById<EditText>(R.id.input_keterangan)
        val etNom = dialog.findViewById<EditText>(R.id.input_nominal)
        val etTgl = dialog.findViewById<EditText>(R.id.input_tanggal)
        val spinnerKat = dialog.findViewById<Spinner>(R.id.spinner_kategori)

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.pilihan_kategori,
            R.layout.spinner_item
        )

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        spinnerKat.adapter = adapter

        btnSimpan.setOnClickListener {
            val nominalRaw = etNom.text.toString().replace("[Rp,. ]".toRegex(), "")
            val tanggalRaw = etTgl.text.toString() // DD/MM/YYYY

            if (etKet.text.isNotEmpty() && nominalRaw.isNotEmpty() && tanggalRaw.length == 10) {
                // Pecah tanggal untuk ambil bulan & tahun
                val parts = tanggalRaw.split("/")
                if (parts.size < 3) {
                    Toast.makeText(this, "Format tanggal salah", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val tglBulan = parts[1].toIntOrNull() ?: return@setOnClickListener
                val tglTahun = parts[2].toIntOrNull() ?: return@setOnClickListener

                val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
                val email = prefs.getString("userEmail", "offline") ?: "offline"

                val dataBaru = Pengeluaran(
                    keterangan = etKet.text.toString(),
                    kategori = spinnerKat.selectedItem.toString(),
                    nominal = nominalRaw.toLong(),
                    tanggal = tanggalRaw,
                    bulan = tglBulan,
                    tahun = tglTahun,
                    bukti = buktiUri?.toString(),
                    userEmail = email
                )


                // Simpan ke Room (Gunakan Coroutine)
                lifecycleScope.launch(Dispatchers.IO) {

                    repo.insert(dataBaru)

                    withContext(Dispatchers.Main) {
                        dialog.dismiss()
                        Toast.makeText(this@MainActivity, "Tersimpan", Toast.LENGTH_SHORT).show()

                        if (tglBulan == currentMonthView && tglTahun == currentYearView) {
                            recreateObserver()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Lengkapi semua data", Toast.LENGTH_SHORT).show()
            }
        }

        // Memfungsikan tombol silang (Close)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btn_close_dialog)
        btnClose?.setOnClickListener {
            dialog.dismiss()
        }

        val etTanggal = dialog.findViewById<EditText>(R.id.input_tanggal)
        etTanggal?.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true

                // Hapus semua garis miring saat ini untuk menghitung ulang angkanya
                var str = s.toString().replace("/", "")

                // Batasi panjang angka maksimal 8 digit (DDMMYYYY)
                if (str.length > 8) {
                    str = str.substring(0, 8)
                }

                val formatted = StringBuilder()
                for (i in str.indices) {
                    formatted.append(str[i])
                    // Sisipkan '/' setelah mengetik 2 digit (Hari) dan 4 digit (Bulan)
                    if ((i == 1 || i == 3) && i != str.length - 1) {
                        formatted.append("/")
                    }
                }

                // Tampilkan teks yang sudah diformat ke EditText
                etTanggal.setText(formatted.toString())
                // Pindahkan kursor ke paling belakang agar user bisa lanjut mengetik
                etTanggal.setSelection(formatted.length)

                isUpdating = false
            }
        })

        // 1. Cari ID tombol kalender
        val btnKalender = dialog.findViewById<ImageButton>(R.id.btn_kalender)
        val kalender = Calendar.getInstance().apply {
            set(Calendar.MONTH, currentMonthView - 1)
            set(Calendar.YEAR, currentYearView)
        }

        // 2. Set klik listener pada IKON KALENDER, BUKAN pada EditText
        btnKalender?.setOnClickListener {
            val datePicker = DatePickerDialog(this, { _, year, month, day ->
                // Format tanggal: DD/MM/YYYY
                val tanggalTerpilih = String.format("%02d/%02d/%d", day, month + 1, year)
                etTanggal.setText(tanggalTerpilih)
            },
                kalender.get(Calendar.YEAR),
                kalender.get(Calendar.MONTH),
                kalender.get(Calendar.DAY_OF_MONTH))

            datePicker.show()
        }

        val etNominal = dialog.findViewById<EditText>(R.id.input_nominal)

        etNominal?.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    etNominal.removeTextChangedListener(this)

                    // Bersihkan semua karakter kecuali angka
                    val cleanString = s.toString().replace("[Rp,. ]".toRegex(), "")

                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        // Format menjadi Rupiah (Locale Indonesia)
                        val formatted = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(parsed)

                        // Hapus simbol desimal ",00" jika ada (opsional)
                        val finalFormat = formatted.replace(",00", "").replace("Rp", "Rp ")

                        current = finalFormat
                        etNominal.setText(finalFormat)
                        etNominal.setSelection(finalFormat.length) // Kursor tetap di akhir
                    } else {
                        current = ""
                        etNominal.setText("")
                    }

                    etNominal.addTextChangedListener(this)
                }
            }
        })

        btnClose?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )

        dialog.show()

        }

    private fun expand(view: View) {
        view.measure(
            View.MeasureSpec.makeMeasureSpec((view.parent as View).width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.UNSPECIFIED
        )

        val targetHeight = view.measuredHeight

        val animator = android.animation.ValueAnimator.ofInt(0, targetHeight)
        animator.duration = 1000
        animator.interpolator = android.view.animation.DecelerateInterpolator()

        view.layoutParams.height = 0
        view.visibility = View.VISIBLE

        animator.addUpdateListener {
            val value = it.animatedValue as Int
            view.layoutParams.height = value
            view.requestLayout()
        }

        animator.start()
    }

    private fun collapse(view: View) {
        val initialHeight = view.measuredHeight

        val animator = android.animation.ValueAnimator.ofInt(initialHeight, 0)
        animator.duration = 1000
        animator.interpolator = android.view.animation.AccelerateInterpolator()

        animator.addUpdateListener {
            val value = it.animatedValue as Int
            view.layoutParams.height = value
            view.requestLayout()
        }

        animator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                view.visibility = View.GONE
            }
        })

        animator.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        firestoreListener?.remove()
    }

    }


