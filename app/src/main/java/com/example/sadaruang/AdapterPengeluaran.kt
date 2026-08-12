package com.example.sadaruang

import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class AdapterPengeluaran(
    private val onDeleteClick: (Pengeluaran) -> Unit
) : RecyclerView.Adapter<AdapterPengeluaran.ViewHolder>() {

    // Fungsi untuk memperbarui data di list

    private var data = listOf<Pengeluaran>()

    fun setData(newList: List<Pengeluaran>) {
        data = newList
        notifyDataSetChanged()
    }

    // ... sisa kode lainnya sudah benar ...

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvKategori: TextView = view.findViewById(R.id.tv_kategori)
        val ivDelete: ImageView = view.findViewById(R.id.iv_delete)
        val ivBukti: ImageView = view.findViewById(R.id.iv_bukti)
        val ivKategori: ImageView = view.findViewById(R.id.iv_kategori)
        val tvKeterangan: TextView = view.findViewById(R.id.tv_keterangan)
        val tvTanggal: TextView = view.findViewById(R.id.tv_tanggal_item)
        val tvNominal: TextView = view.findViewById(R.id.tv_nominal_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pengeluaran, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        // 1. Set Teks Keterangan & Tanggal
        holder.tvKeterangan.text = item.keterangan
        holder.tvTanggal.text = item.tanggal
        holder.tvKategori.text = item.kategori

        holder.ivDelete.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Hapus")
                .setMessage("Yakin mau hapus?")
                .setPositiveButton("Ya") { _, _ ->
                    onDeleteClick(item)
                }
                .setNegativeButton("Batal", null)
                .show()
        }


        // RESET DULU (WAJIB)
        holder.ivBukti.visibility = View.GONE
        holder.ivBukti.setOnClickListener(null)

// BARU SET DATA
        if (!item.bukti.isNullOrEmpty()) {
            holder.ivBukti.visibility = View.VISIBLE

            holder.ivBukti.setOnClickListener {
                val context = holder.itemView.context
                val dialog = Dialog(context)
                dialog.setContentView(R.layout.dialog_preview_image)

                val imageView = dialog.findViewById<com.github.chrisbanes.photoview.PhotoView>(R.id.iv_full_image)

                try {
                    item.bukti?.let {
                        Glide.with(context)
                            .load(it)
                            .into(imageView)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Gagal membuka gambar", Toast.LENGTH_SHORT).show()
                }

                dialog.show()
            }
        }

        // 2. Format Nominal ke Rupiah
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
        val nominalFormatted = formatRupiah.format(item.nominal).replace(",00", "")
        holder.tvNominal.text = "$nominalFormatted"

        // 3. Set Ikon berdasarkan Kategori (Bisa kamu sesuaikan nama kategorinya nanti)
        when (item.kategori.lowercase()) {
            "makanan" -> holder.ivKategori.setImageResource(R.drawable.ic_makanan)
            "transportasi" -> holder.ivKategori.setImageResource(R.drawable.ic_transport)
            "hiburan" -> holder.ivKategori.setImageResource(R.drawable.ic_hiburan)
            else -> holder.ivKategori.setImageResource(R.drawable.kategori)
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }
}