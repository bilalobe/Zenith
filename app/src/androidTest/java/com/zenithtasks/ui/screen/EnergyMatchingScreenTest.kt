package com.zenithtasks.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.zenithtasks.data.local.entity.EnergyLevel
import com.zenithtasks.domain.model.Task
import com.zenithtasks.ui.theme.ZenithTheme
import com.zenithtasks.ui.viewmodel.EnergyMatchingViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class EnergyMatchingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun energyLevelSelectorDisplaysAllOptions() {
        // Given
        val viewModel = mockk<EnergyMatchingViewModel>(relaxed = true)
        every { viewModel.userEnergyLevel } returns MutableStateFlow(EnergyLevel.MEDIUM)
        every { viewModel.energyFilteredTasks } returns MutableStateFlow(emptyList())
        every { viewModel.suggestedTasks } returns MutableStateFlow(emptyList())
        
        // When
        composeTestRule.setContent {
            ZenithTheme {
                EnergyMatchingScreen(viewModel = viewModel)
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("How's your energy today?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Low Energy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Medium Energy").assertIsDisplayed()
        composeTestRule.onNodeWithText("High Energy").assertIsDisplayed()
    }
    
    @Test
    fun selectingEnergyLevelCallsViewModel() {
        // Given
        val viewModel = mockk<EnergyMatchingViewModel>(relaxed = true)
        every { viewModel.userEnergyLevel } returns MutableStateFlow(EnergyLevel.MEDIUM)
        every { viewModel.energyFilteredTasks } returns MutableStateFlow(emptyList())
        every { viewModel.suggestedTasks } returns MutableStateFlow(emptyList())
        
        // When
        composeTestRule.setContent {
            ZenithTheme {
                EnergyMatchingScreen(viewModel = viewModel)
            }
        }
        
        // Then click on the Low Energy option
        composeTestRule.onNodeWithText("Low Energy").performClick()
        
        // Verify the ViewModel was called with the right energy level
        verify { viewModel.setUserEnergyLevel(EnergyLevel.LOW) }
    }
    
    @Test
    fun displaysSuggestedTasksWhenAvailable() {
        // Given
        val viewModel = mockk<EnergyMatchingViewModel>(relaxed = true)
        val suggestedTasks = listOf(
            Task(
                id = 1,
                title = "Suggested Task",
                description = "This is a suggested task",
                energyLevel = EnergyLevel.LOW,
                createdAt = LocalDateTime.now()
            )
        )
        
        every { viewModel.userEnergyLevel } returns MutableStateFlow(EnergyLevel.MEDIUM)
        every { viewModel.energyFilteredTasks } returns MutableStateFlow(emptyList())
        every { viewModel.suggestedTasks } returns MutableStateFlow(suggestedTasks)
        
        // When
        composeTestRule.setContent {
            ZenithTheme {
                EnergyMatchingScreen(viewModel = viewModel)
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Suggested for your energy level").assertIsDisplayed()
        composeTestRule.onNodeWithText("Suggested Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("This is a suggested task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Low").assertIsDisplayed() // Energy level indicator
    }
    
    @Test
    fun displaysNoTasksMessageWhenNoTasks() {
        // Given
        val viewModel = mockk<EnergyMatchingViewModel>(relaxed = true)
        every { viewModel.userEnergyLevel } returns MutableStateFlow(EnergyLevel.MEDIUM)
        every { viewModel.energyFilteredTasks } returns MutableStateFlow(emptyList())
        every { viewModel.suggestedTasks } returns MutableStateFlow(emptyList())
        
        // When
        composeTestRule.setContent {
            ZenithTheme {
                EnergyMatchingScreen(viewModel = viewModel)
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("All matching tasks").assertIsDisplayed()
        composeTestRule.onNodeWithText("No tasks found for your current energy level").assertIsDisplayed()
    }
}