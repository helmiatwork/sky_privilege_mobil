package com.skyprivilege.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.skyprivilege.ui.GrabGreen

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SkyPullRefreshBox(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    indicatorColor: Color = GrabGreen,
    content: @Composable BoxScope.() -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = onRefresh
    )

    Box(
        modifier = modifier.pullRefresh(pullRefreshState)
    ) {
        content()

        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Color.White,
            contentColor = indicatorColor
        )
    }
}
