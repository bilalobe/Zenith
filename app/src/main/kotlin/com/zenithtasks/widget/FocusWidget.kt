package com.zenithtasks.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.zenithtasks.R
import com.zenithtasks.data.repository.FocusSessionRepository
import com.zenithtasks.di.RepositoryEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch


class FocusWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    // Preference key for focus state
    companion object {
        val IS_FOCUS_ACTIVE = booleanPreferencesKey("is_focus_active")
        const val TAG = "FocusWidget"
    }

    sealed class WidgetResult {
        data class Success(val focusSessionRepository: FocusSessionRepository) : WidgetResult()
        data class Error(val message: String) : WidgetResult()
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val result = try {
                val appContext = context.applicationContext
                val hiltEntryPoint = EntryPointAccessors.fromApplication(
                    appContext,
                    RepositoryEntryPoint::class.java
                )
                val focusSessionRepository = hiltEntryPoint.focusSessionRepository()
                WidgetResult.Success(focusSessionRepository)
            } catch (e: Exception) {
                Log.e(TAG, "Error providing widget content", e)
                WidgetResult.Error("Something went wrong")
            }

            when (result) {
                is WidgetResult.Success -> FocusWidgetContent(result.focusSessionRepository)
                is WidgetResult.Error -> ErrorWidgetContent()
            }
        }
    }

    @Composable
    private fun FocusWidgetContent(focusSessionRepository: FocusSessionRepository) {
        val prefs = currentState<Preferences>()
        val isFocusActive = prefs[IS_FOCUS_ACTIVE] == false
        val context = LocalContext.current
        
        updateFocusState(context, isFocusActive, focusSessionRepository)

        val backgroundColor = if (isFocusActive)
            ColorProvider(day = Color(0xFF3949AB), night = Color(0xFF3949AB))
        else
            ColorProvider(day = Color(0xFF424242), night = Color(0xFF424242))

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(backgroundColor)
                .padding(12.dp)
                .cornerRadius(8.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Image(
                    provider = ImageProvider(
                        if (isFocusActive) R.drawable.ic_focus_active else R.drawable.ic_focus_inactive
                    ),
                    contentDescription = "Focus Icon",
                    modifier = GlanceModifier.size(32.dp)
                )
                
                Spacer(modifier = GlanceModifier.height(8.dp))
                
                Text(
                    text = if (isFocusActive) "FOCUS ACTIVE" else "FOCUS INACTIVE",
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFFFFFFFF)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.fillMaxWidth(),
                    maxLines = 1
                )
                
                Spacer(modifier = GlanceModifier.height(12.dp))
                
                Button(
                    text = if (isFocusActive) "END FOCUS" else "START FOCUS",
                    onClick = actionRunCallback<FocusActionCallback>(),
                    modifier = GlanceModifier.cornerRadius(8.dp)
                )
            }
        }
    }

    @Composable
    private fun ErrorWidgetContent() {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(ColorProvider(day = Color(0xFF424242), night = Color(0xFF424242)))
                .padding(12.dp)
                .cornerRadius(8.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Text(
                    text = "Widget Error",
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFFFFFFFF)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.fillMaxWidth(),
                    maxLines = 1
                )
                
                Spacer(modifier = GlanceModifier.height(8.dp))
                
                Text(
                    text = "Tap to reload",
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFFFFFFFF)),
                        fontSize = 14.sp
                    ),
                    modifier = GlanceModifier.fillMaxWidth(),
                    maxLines = 1
                )
                
                Spacer(modifier = GlanceModifier.height(12.dp))
                
                Button(
                    text = "RELOAD",
                    onClick = actionRunCallback<FocusActionCallback>(),
                    modifier = GlanceModifier.cornerRadius(8.dp)
                )
            }
        }
    }

    private fun updateFocusState(
        context: Context,
        isFocusActive: Boolean,
        focusSessionRepository: FocusSessionRepository
    ) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                focusSessionRepository.getActiveFocusSession()
                    .catch { e ->
                        Log.e(TAG, "Error fetching focus session", e)
                    }
                    .collect { activeSession ->
                        if (isFocusActive != (activeSession != null)) {
                            // Only update if there's a mismatch between widget state and actual state
                            val widgetIds = GlanceAppWidgetManager(context = context)
                                .getGlanceIds(FocusWidget::class.java)
                            
                            widgetIds.forEach { glanceId ->
                                updateAppWidgetState(context, glanceId) { prefs ->
                                    prefs[IS_FOCUS_ACTIVE] = activeSession != null
                                }
                                update(context, glanceId)
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating focus state", e)
            }
        }
    }
}
