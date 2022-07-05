package com.inspiredandroid.linuxcommandbibliotheca.ui.bars

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.inspiredandroid.linuxcommandbibliotheca.BookmarkManager
import com.inspiredandroid.linuxcommandbibliotheca.R
import com.inspiredandroid.linuxcommandbibliotheca.ui.screens.AppInfoDialog
import org.example.common.databaseHelper
import org.example.common.getHtmlFileName

/* Copyright 2022 Simon Schubert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

@Composable
fun TopBar(
    navBackStackEntry: State<NavBackStackEntry?>,
    textFieldValue: MutableState<TextFieldValue>,
    onNavigateBack: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }

    val route = navBackStackEntry.value?.destination?.route
    if (route == "commands") {
        val showSearch = remember { mutableStateOf(true) }

        TopAppBar(title = { Text("Commands") },
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = Color.White,
            navigationIcon = if (showSearch.value) {
                {
                    IconButton(onClick = {
                        textFieldValue.value = TextFieldValue("")
                        showSearch.value = false
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            modifier = Modifier,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            } else {
                null
            }, actions = {
                if (showSearch.value) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .focusRequester(focusRequester),
                        value = textFieldValue.value,
                        onValueChange = { tfv ->
                            if (showSearch.value) {
                                textFieldValue.value = tfv
                            }
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            backgroundColor = Color.Transparent,
                            cursorColor = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
                        ),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (textFieldValue.value.text.isEmpty()) {
                                    showSearch.value = false
                                } else {
                                    textFieldValue.value = TextFieldValue("")
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(id = R.string.reset)
                                )
                            }
                        },
                        maxLines = 1,
                        singleLine = true
                    )
                } else {
                    IconButton(onClick = {
                        showSearch.value = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.search)
                        )
                    }
                }
            })
        if (showSearch.value) {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    } else {
        val showBackIcon = when (route) {
            "basics", "tips" -> false
            else -> true
        }
        val showAppInfoIcon = route == "basics"
        val showBookmarkIcon = route?.startsWith("command?") ?: false
        val showDialog = remember { mutableStateOf(false) }

        TopAppBar(
            title = {
                Text(getTitleByRoute(navBackStackEntry.value), overflow = TextOverflow.Ellipsis)
            },
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = Color.White,
            navigationIcon = if (showBackIcon) {
                {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            } else {
                null
            },
            actions = {
                if (showAppInfoIcon) {
                    IconButton(onClick = {
                        showDialog.value = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = stringResource(R.string.info)
                        )
                    }
                }
                if (showBookmarkIcon) {
                    val commandName = navBackStackEntry.value?.arguments?.getString("commandName") ?: ""
                    val command = databaseHelper.getCommand(commandName)
                    if (command != null) {
                        val context = LocalContext.current
                        val bookmarkManager = BookmarkManager()
                        val isBookmarked = remember { mutableStateOf(bookmarkManager.hasBookmark(context, command.id)) }
                        IconButton(onClick = {
                            isBookmarked.value = !isBookmarked.value
                            if (isBookmarked.value) {
                                bookmarkManager.addBookmark(context, command.id)
                            } else {
                                bookmarkManager.removeBookmark(context, command.id)
                            }
                        }) {
                            Icon(
                                if (isBookmarked.value) {
                                    painterResource(R.drawable.ic_bookmark_black_24dp)
                                } else {
                                    painterResource(R.drawable.ic_bookmark_border_black_24dp)
                                },
                                contentDescription = if (isBookmarked.value) {
                                    stringResource(R.string.bookmarked)
                                } else {
                                    stringResource(R.string.bookmark)
                                }
                            )
                        }
                    }
                }
            }
        )
        AppInfoDialog(showDialog)
    }
}


@Composable
private fun getTitleByRoute(backStackEntry: NavBackStackEntry?): String {
    if (backStackEntry == null) {
        return "Linux"
    }
    return when (val route = backStackEntry.destination.route) {
        "commands" -> stringResource(R.string.commands)
        "basics" -> stringResource(R.string.basics)
        "tips" -> stringResource(R.string.tips)
        else -> {
            if (route?.startsWith("command?") == true) {
                backStackEntry.arguments?.getString("commandName") ?: ""
            } else if (route?.startsWith("basicgroups?") == true) {
                val deeplinkName = backStackEntry.arguments?.getString("deepLinkCategoryName") ?: ""
                if (deeplinkName.isEmpty()) {
                    backStackEntry.arguments?.getString("categoryName") ?: ""
                } else {
                    val categories = databaseHelper.getBasics()
                    val category = categories.firstOrNull { it.getHtmlFileName() == deeplinkName }
                    category?.title ?: "Not found"
                }
            } else {
                ""
            }
        }
    }
}
