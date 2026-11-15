package com.example.onepass.presentation.activity

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onepass.R
import com.example.onepass.utils.HistoryLog
import com.example.onepass.utils.LogHistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogHistoryActivity : AppCompatActivity() {
    private lateinit var editSearch: EditText
    private lateinit var textLogCount: TextView
    private lateinit var recyclerViewLogs: RecyclerView
    private lateinit var textNoLogs: TextView

    private val allLogs = mutableListOf<LogHistoryEntry>()
    private lateinit var adapter: LogHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_history)

        editSearch = findViewById(R.id.editLogSearch)
        textLogCount = findViewById(R.id.textLogCount)
        recyclerViewLogs = findViewById(R.id.recyclerViewLogs)
        textNoLogs = findViewById(R.id.textNoLogs)

        adapter = LogHistoryAdapter()
        recyclerViewLogs.layoutManager = LinearLayoutManager(this)
        recyclerViewLogs.adapter = adapter

        editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterLogs(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        loadLogs()
    }

    private fun loadLogs() {
        allLogs.clear()
        allLogs.addAll(HistoryLog.getLogs())
        filterLogs(editSearch.text?.toString().orEmpty())
    }

    private fun filterLogs(query: String) {
        val trimmed = query.trim()
        val filtered = if (trimmed.isEmpty()) {
            allLogs
        } else {
            allLogs.filter {
                it.tag.contains(trimmed, ignoreCase = true) ||
                    it.message.contains(trimmed, ignoreCase = true) ||
                    it.level.contains(trimmed, ignoreCase = true)
            }
        }

        adapter.submit(filtered)
        textLogCount.text = "共 ${filtered.size} 条日志"
        textNoLogs.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        recyclerViewLogs.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private class LogHistoryAdapter : RecyclerView.Adapter<LogHistoryAdapter.ViewHolder>() {
        private val items = mutableListOf<LogHistoryEntry>()
        private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        fun submit(newItems: List<LogHistoryEntry>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_log_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position], timeFormatter)
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val textMeta: TextView = itemView.findViewById(R.id.textLogMeta)
            private val textMessage: TextView = itemView.findViewById(R.id.textLogMessage)

            fun bind(entry: LogHistoryEntry, formatter: SimpleDateFormat) {
                val time = formatter.format(Date(entry.timestamp))
                textMeta.text = "$time  ${entry.level}/${entry.tag}"
                textMessage.text = entry.message
                textMeta.setTextColor(
                    when (entry.level) {
                        "E" -> Color.parseColor("#C62828")
                        "W" -> Color.parseColor("#EF6C00")
                        "I" -> Color.parseColor("#1565C0")
                        else -> Color.parseColor("#333333")
                    }
                )
            }
        }
    }
}
