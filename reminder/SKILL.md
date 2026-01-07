---
name: reminder
description: Play audio alerts via the reminder CLI when Codex finishes a task, encounters an error/abort, or needs user help; use in WSL environments with the reminder-tool audio prompts and map events to TASK_FINISHED, ERROR, or NEED_HELP.
---

# Reminder

## Overview

Use the `reminder` CLI to play short MP3 alerts for task completion, errors, or help-needed moments. Keep usage optional and non-intrusive.

## Workflow

1. Determine the event type.
   - Task completed successfully -> `TASK_FINISHED`
   - Task failed or aborted -> `ERROR`
   - Waiting for user input or blocked -> `NEED_HELP`
2. Play the matching sound once (avoid repeated alerts for the same event).
3. If the CLI is unavailable or playback fails, continue without blocking and optionally mention the missing dependency.

## Command usage

Prefer a built binary in PATH:

```bash
reminder -type TASK_FINISHED
```

If using the bundled assets, keep `reminder` and `audio/` together and run the binary directly:

```bash
./reminder -type TASK_FINISHED
```

If running inside the reminder-tool repo, use Go directly:

```bash
go run . -type NEED_HELP
```

## Notes

- The CLI looks for `audio/<TYPE>.mp3` next to the executable; if not found, it falls back to `./audio/<TYPE>.mp3` from the current working directory.
- Audio playback requires WSL audio support and a player such as `ffplay`, `mpg123`, or `mpv`.
