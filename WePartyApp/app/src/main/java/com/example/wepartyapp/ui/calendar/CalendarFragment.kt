package com.example.wepartyapp.ui.calendar

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.EventViewModel
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.CalendarView
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    private val sharedViewModel: EventViewModel by activityViewModels()

    private var selectedDate: LocalDate = LocalDate.now()
    private var eventDate: LocalDate? = null

    // Formatters
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- FIND VIEWS ---
        val tvEventName = view.findViewById<TextView>(R.id.tv_event_name)
        val tvEventTime = view.findViewById<TextView>(R.id.tv_event_time)
        val tvMapsLink = view.findViewById<TextView>(R.id.tv_maps_link)
        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)

        // Navigation Views (Arrows & Title)
        val tvMonthTitle = view.findViewById<TextView>(R.id.tv_month_title)
        val btnPrevMonth = view.findViewById<ImageView>(R.id.btn_prev_month)
        val btnNextMonth = view.findViewById<ImageView>(R.id.btn_next_month)

        // Visibility Groups
        val topInfoGroup = view.findViewById<LinearLayout>(R.id.layout_info_top)
        val bottomInfoGroup = view.findViewById<LinearLayout>(R.id.layout_info_bottom)

        // --- OBSERVE DATA ---
        sharedViewModel.eventName.observe(viewLifecycleOwner) { tvEventName.text = it }
        sharedViewModel.eventAddress.observe(viewLifecycleOwner) { address ->
            tvMapsLink.text = address
            tvMapsLink.setOnClickListener { openGoogleMaps(address) }
        }

        sharedViewModel.eventTime.observe(viewLifecycleOwner) { timeString ->
            tvEventTime.text = "Time: $timeString"
            try {
                // Parse date string (e.g. "Feb 20, 2026")
                val datePart = timeString.split("@")[0].trim()
                eventDate = LocalDate.parse(datePart, dateFormatter)

                calendarView.notifyCalendarChanged()
                updateDetailsVisibility(topInfoGroup, bottomInfoGroup)
            } catch (e: Exception) { }
        }

        // --- CALENDAR LOGIC ---
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                val day = data.date
                container.textView.text = day.dayOfMonth.toString()

                if (data.position != DayPosition.MonthDate) {
                    container.textView.setTextColor(Color.GRAY)
                    container.textView.background = null
                    return
                }

                // Balloon Colors
                when (day) {
                    selectedDate -> {
                        container.textView.setTextColor(Color.WHITE)
                        container.textView.setBackgroundResource(R.drawable.bg_circle_blue)
                    }
                    eventDate -> {
                        container.textView.setTextColor(Color.WHITE)
                        container.textView.setBackgroundResource(R.drawable.bg_circle_green)
                    }
                    else -> {
                        container.textView.setTextColor(Color.BLACK)
                        container.textView.background = null
                    }
                }

                // Click Listener
                container.view.setOnClickListener {
                    val oldDate = selectedDate
                    selectedDate = day
                    calendarView.notifyDateChanged(day)
                    if (oldDate != null) calendarView.notifyDateChanged(oldDate)

                    updateDetailsVisibility(topInfoGroup, bottomInfoGroup)
                }
            }
        }

        // --- MONTH NAVIGATION LOGIC ---
        // 1. Update Title when scrolling
        calendarView.monthScrollListener = { month ->
            tvMonthTitle.text = monthTitleFormatter.format(month.yearMonth)
        }

        // 2. Click Listeners for Arrows
        btnNextMonth.setOnClickListener {
            calendarView.findFirstVisibleMonth()?.let {
                calendarView.smoothScrollToMonth(it.yearMonth.plusMonths(1))
            }
        }

        btnPrevMonth.setOnClickListener {
            calendarView.findFirstVisibleMonth()?.let {
                calendarView.smoothScrollToMonth(it.yearMonth.minusMonths(1))
            }
        }

        // Setup Range
        val currentMonth = YearMonth.now()
        calendarView.setup(currentMonth.minusYears(2), currentMonth.plusYears(2), DayOfWeek.SUNDAY)
        calendarView.scrollToMonth(currentMonth)

        updateDetailsVisibility(topInfoGroup, bottomInfoGroup)
    }

    // Toggle Groups (Invisible/Visible)
    private fun updateDetailsVisibility(topGroup: View, bottomGroup: View) {
        val isEventDay = (selectedDate == eventDate)
        val newState = if (isEventDay) View.VISIBLE else View.INVISIBLE

        topGroup.visibility = newState
        bottomGroup.visibility = newState
    }

    private fun openGoogleMaps(address: String) {
        val gmmIntentUri = "geo:0,0?q=${Uri.encode(address)}".toUri()
        try {
            startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri).setPackage("com.google.android.apps.maps"))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
        }
    }

    class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
    }
}