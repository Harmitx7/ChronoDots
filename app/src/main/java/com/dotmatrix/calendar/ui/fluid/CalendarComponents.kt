package com.dotmatrix.calendar.ui.fluid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun FluidCalendarScreen() {
    val currentDate = remember { LocalDate.now() }
    val currentYear = currentDate.year
    val scrollState = rememberScrollState()
    
    // Animation states
    var showHeader by remember { mutableStateOf(false) }
    var showProgress by remember { mutableStateOf(false) }
    var showYearView by remember { mutableStateOf(false) }
    var showMonthView by remember { mutableStateOf(false) }
    
    // Staggered animation trigger
    LaunchedEffect(Unit) {
        showHeader = true
        delay(100)
        showProgress = true
        delay(150)
        showYearView = true
        delay(150)
        showMonthView = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FluidTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            // Header with animation
            AnimatedVisibility(
                visible = showHeader,
                enter = fadeIn(animationSpec = tween(400)) + 
                        slideInVertically(animationSpec = tween(400)) { -40 }
            ) {
                CalendarHeader(year = currentYear)
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Year Progress Card with animation
            AnimatedVisibility(
                visible = showProgress,
                enter = fadeIn(animationSpec = tween(500)) + 
                        slideInVertically(animationSpec = tween(500)) { 60 }
            ) {
                YearProgressCard(currentDate = currentDate)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Year View with animation
            AnimatedVisibility(
                visible = showYearView,
                enter = fadeIn(animationSpec = tween(600)) + 
                        slideInVertically(animationSpec = tween(600)) { 80 }
            ) {
                YearDotMatrixView(year = currentYear, currentDate = currentDate)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Current Month Detail with animation
            AnimatedVisibility(
                visible = showMonthView,
                enter = fadeIn(animationSpec = tween(700)) + 
                        slideInVertically(animationSpec = tween(700)) { 100 }
            ) {
                MonthDetailCard(initialYearMonth = YearMonth.from(currentDate), currentDate = currentDate)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CalendarHeader(year: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "ChronoDots",
                color = FluidTheme.colors.secondaryText.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Text(
                text = year.toString(),
                color = FluidTheme.colors.primaryText,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            )
        }
        
        IconButton(
            onClick = { /* Settings */ },
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(FluidTheme.colors.surface.copy(alpha = 0.5f))
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                tint = FluidTheme.colors.primaryText.copy(alpha = 0.8f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun YearProgressCard(currentDate: LocalDate) {
    val dayOfYear = currentDate.dayOfYear
    val totalDays = if (currentDate.isLeapYear) 366 else 365
    val progress = dayOfYear.toFloat() / totalDays
    val percentComplete = (progress * 100).toInt()
    
    // Animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "progress"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFFE0E0E0), // Light border
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White // STRICTLY WHITE
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated progress dots
            AnimatedDotProgressBar(progress = animatedProgress)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Day $dayOfYear of $totalDays",
                    color = Color(0xFF666666), // Dark Gray
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$percentComplete%",
                    color = Color(0xFF1A1A1A), // Black
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AnimatedDotProgressBar(progress: Float) {
    val dotCount = 20
    val filledDots = maxOf(1, (progress * dotCount).toInt()) // At least 1 filled dot
    val currentDotIndex = filledDots - 1
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 0 until dotCount) {
            val isFilled = i < filledDots
            val isCurrentDot = i == currentDotIndex
            
            val dotColor = when {
                isCurrentDot -> Color(0xFFFB6905) // Orange
                isFilled -> Color(0xFFFB6905).copy(alpha = 0.85f)
                else -> Color(0xFFE0E0E0) // Light Gray
            }
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(if (isCurrentDot) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = if (isCurrentDot) pulseAlpha else dotColor.alpha))
            )
        }
    }
}

@Composable
private fun YearDotMatrixView(year: Int, currentDate: LocalDate) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val months = listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
                months.forEach { month ->
                    Text(
                        text = month,
                        color = Color(0xFF666666),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (month in 1..12) {
                    MonthDotColumn(
                        yearMonth = YearMonth.of(year, month),
                        currentDate = currentDate,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthDotColumn(
    yearMonth: YearMonth,
    currentDate: LocalDate,
    modifier: Modifier = Modifier
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        for (day in 1..31) {
            if (day <= daysInMonth) {
                val date = yearMonth.atDay(day)
                val isToday = date == currentDate
                val isPast = date.isBefore(currentDate)
                
                val dotColor = when {
                    isToday -> Color(0xFFFB6905)
                    isPast -> Color(0xFF888888)
                    else -> Color(0xFFE0E0E0)
                }
                
                val dotSize = when {
                    isToday -> 7.dp
                    else -> 5.dp
                }
                
                Box(
                    modifier = Modifier
                        .padding(vertical = 1.dp)
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            } else {
                Box(
                    modifier = Modifier
                        .padding(vertical = 1.dp)
                        .size(5.dp)
                        .alpha(0f)
                )
            }
        }
    }
}

// Fixed Clean UI Calendar Card
@Composable
private fun MonthDetailCard(initialYearMonth: YearMonth, currentDate: LocalDate) {
    var displayedYearMonth by remember { mutableStateOf(initialYearMonth) }
    
    val fullYearString = displayedYearMonth.year.toString()
    val monthName = displayedYearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFFE0E0E0), // Light border
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White // STRICTLY WHITE as requested
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header with navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { displayedYearMonth = displayedYearMonth.minusMonths(1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month",
                        tint = Color(0xFF1A1A1A)
                    )
                }
                
                Text(
                    text = "$fullYearString $monthName",
                    color = Color(0xFF1A1A1A),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                
                IconButton(
                    onClick = { displayedYearMonth = displayedYearMonth.plusMonths(1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month",
                        tint = Color(0xFF1A1A1A)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Week day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val days = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                days.forEach { day ->
                    Text(
                        text = day,
                        color = Color.Black, // Bold black for headers
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Calendar grid
            CleanMonthCalendarGrid(
                yearMonth = displayedYearMonth, 
                currentDate = currentDate
            )
        }
    }
}

@Composable
private fun CleanMonthCalendarGrid(yearMonth: YearMonth, currentDate: LocalDate) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    val startOffset = if (firstDayOfMonth.dayOfWeek.value == 7) 0 else firstDayOfMonth.dayOfWeek.value
    
    val totalCells = startOffset + daysInMonth
    val weeks = (totalCells + 6) / 7
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (week in 0 until weeks) {
            val weekStartDayIndex = week * 7 - startOffset + 1
            val weekEndDayIndex = weekStartDayIndex + 6
            
            // Check if this week contains the selected date (simulating 'today' or selected)
            // Logic: Is today within this week's range AND are we in the current month?
            val isCurrentMonth = yearMonth.month == currentDate.month && yearMonth.year == currentDate.year
            val containsSelectedDate = isCurrentMonth && (currentDate.dayOfMonth in weekStartDayIndex..weekEndDayIndex)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (containsSelectedDate) Color(0xFFF7F7F7) else Color.Transparent), // Week highlight
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0..6) {
                    val dayIndex = week * 7 + dayOfWeek - startOffset + 1
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp), // Consistent row height
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayIndex in 1..daysInMonth) {
                            val date = yearMonth.atDay(dayIndex)
                            val isSelected = date == currentDate
                            
                            // Day content
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFFFB6905) else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayIndex.toString(),
                                    color = if (isSelected) Color.White else Color(0xFF444444),
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
