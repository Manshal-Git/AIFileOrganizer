package com.manshal79.aifileorganizer.presentation.organizer

/** Opens a native, platform-appropriate folder picker. Returns null if the user cancels. */
expect fun pickDirectory(): String?
