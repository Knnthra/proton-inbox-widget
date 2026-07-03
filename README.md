# Proton Inbox Widget

A home screen widget that shows your recent Proton Mail messages, built by
listening to Proton Mail's notifications (sender + subject). Everything stays
on your device — no network access, no credentials, no permissions beyond
notification access.

## How it works
1. `MailNotificationListener` (a NotificationListenerService) watches for
   notifications from `ch.protonmail.android`, ignoring group summaries and
   service notifications.
2. Each new mail (sender, subject, time) is stored locally via `MailStore`
   (SharedPreferences, capped at 30 items).
3. `InboxWidgetProvider` + `InboxWidgetService` render the list in a
   scrollable home screen widget. Tapping a row or the header opens the
   Proton Mail app. The Clear button empties the list.

## Build & install
1. Open the project in Android Studio (Ladybug or newer).
2. Let Gradle sync, then Run ▶ on your phone (enable USB debugging), or
   Build → Build APK and sideload it.

## Setup on the phone
1. Open the app and tap "Grant notification access", enable it for
   Proton Inbox Widget.
2. Make sure Proton Mail notifications are ON (per-mail notifications, not
   silent/summarized).
3. Long-press your home screen → Widgets → Proton Inbox Widget.

## Known limitations
- Only shows mail that arrived (and produced a notification) after setup.
- Can't sync read/unread state or remove items when you read mail elsewhere.
- Tapping a row opens the Proton Mail app, not the specific message
  (Proton doesn't expose a deep link for that).
- If you use Do Not Disturb modes that suppress notifications entirely,
  those mails won't be captured.
