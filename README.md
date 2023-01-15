# Fullscreen Timer

[![license](https://img.shields.io/github/license/ohmae/fullscreen-timer.svg)](./LICENSE)
[![GitHub release](https://img.shields.io/github/release/ohmae/fullscreen-timer.svg)](https://github.com/ohmae/fullscreen-timer/releases)
[![GitHub issues](https://img.shields.io/github/issues/ohmae/fullscreen-timer.svg)](https://github.com/ohmae/fullscreen-timer/issues)
[![GitHub closed issues](https://img.shields.io/github/issues-closed/ohmae/fullscreen-timer.svg)](https://github.com/ohmae/fullscreen-timer/issues?q=is%3Aissue+is%3Aclosed)

# Intent Control

This app accepts control by Intent from other apps.

## EXTRA_MODE (required type: String)

Specifies the app mode. Required. The command is ignored if not specified.

- CLOCK
- STOPWATCH
- TIMER

## EXTRA_COMMAND (type: String)

Specifies a command. There are the following variations. Ignored if MODE is CLOCK

- START
  - Changes to start state. Ignored if already started. If the count is progressing, it will continue.
- STOP
  - Changes to stop state. Ignored if already stoped. If the count is progressing, it will continue.
- SET
  - Sets the count to the specified value and changes to stop state. Specify the value in EXTRA_TIME. Therefore, EXTRA_TIME must be specified, otherwise the command will be ignored.
- SET_AND_START
  - Sets the count to the specified value and changes to start state. Specify the value in EXTRA_TIME. Therefore, EXTRA_TIME must be specified, otherwise the command will be ignored.

## EXTRA_TIME (type: Long)

Specifies the time when EXTRA_COMMAND is SET or SET_AND_START. Specify the time as a long value in milliseconds.

## Example

To start clock mode.

```kotlin
packageManager.getLaunchIntentForPackage("net.mm2d.timer")?.also {
    it.putExtra("EXTRA_MODE", "CLOCK")
}?.let {
    startActivity(it)
}
```

To start stopwatch mode and count up start from 0 seconds.

```kotlin
packageManager.getLaunchIntentForPackage("net.mm2d.timer")?.also {
    it.putExtra("EXTRA_MODE", "STOPWATCH")
    it.putExtra("EXTRA_COMMAND", "SET_AND_START")
    it.putExtra("EXTRA_TIME", 0L)
}?.let {
    startActivity(it)
}
```

To start timer mode and count down start from 2 minutes.

```kotlin
packageManager.getLaunchIntentForPackage("net.mm2d.timer")?.also {
    it.putExtra("EXTRA_MODE", "TIMER")
    it.putExtra("EXTRA_COMMAND", "SET_AND_START")
    it.putExtra("EXTRA_TIME", 120_000L)
}?.let {
    startActivity(it)
}
```

## Author

大前 良介 (OHMAE Ryosuke)
http://www.mm2d.net/

## License

[MIT License](./LICENSE)
