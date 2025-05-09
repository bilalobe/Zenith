package com.zenithtasks.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenithtasks.ui.component.account.SignInCard
import com.zenithtasks.ui.component.account.SignedInCard
import com.zenithtasks.ui.viewmodel.AccountViewModel
import kotlinx.coroutines.launch

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: AccountViewModel = hiltViewModel()
) {
    val isSignedIn by viewModel.isSignedIn.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val authLoading by viewModel.authLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(authError) {
        authError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Short
                )
            }
            viewModel.clearAuthError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Account & Sync") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (isSignedIn) {
                SignedInCard(
                    email = userEmail ?: "",
                    syncStatus = syncStatus,
                    isSyncing = isSyncing,
                    onSyncClick = { viewModel.syncAll() },
                    onSignOutClick = { viewModel.signOut() }
                )
            } else {
                SignInCard(
                    isLoading = authLoading,
                    onSignIn = { email, password -> viewModel.signIn(email, password) },
                    onCreateAccount = { email, password -> viewModel.createAccount(email, password) }
                )
            }
        }
    }
}