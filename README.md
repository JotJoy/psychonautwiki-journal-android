# PsychonautWiki Journal – Personal Fork

This is a **personal fork** of  
https://github.com/isaakhanimann/psychonautwiki-journal-android

I use this fork as a **local, offline-first journal** with additional
harm-reduction and safety-oriented features that fit my own workflow.

This project is not intended to be a medical app.

---

## Disclaimer

This app does **not** provide medical advice.

- No dosing recommendations
- No safety guarantees
- No treatment guidance

All information is **educational only**, based on publicly available sources
and commonly discussed harm-reduction practices.

Use at your own risk.

---

## What’s different from upstream

This fork adds:

### Harm reduction & safety
- Offline substance articles (PsychonautWiki)
- Clear warning and info callouts
- Commonly reported effects per substance
- Educational comfort / mitigation references
- Phase-aware reminders (hydration, recovery, wind-down)
- Optional break-time tracking

### Journal & calendar
- Hide selected substances from calendar views
- Custom duration overrides (onset / peak / total)
- Custom dosage defaults (no advice)
- Fully supported user-added substances

### Data & content
- Manual article caching for offline use
- Resume-safe downloads with progress
- Storage usage overview and cache controls

### Tapering
- Standalone tapering calculator
- Progress tracking
- Visual taper overview
- Optional reminders

All features are opt-in and non-prescriptive.

---

## Privacy

- All data stays **on-device**
- No accounts
- No analytics
- No background network activity

All downloads are explicitly triggered by the user.

---

## AI usage

Some parts of this fork were built with the help of **Antigravity AI**
as a coding assistant. All decisions and reviews were done manually.

---

## Planned (v1.1, not included yet)

- Substance stock tracking
- Test kit inventory
- Saved test results per batch
- Encrypted export / import
- Optional app lock
- Calculate approximate nicotine intake from Cigarette/Cigar weight
- Calculate very approximate THC Content of Joint
- Calculate current BAC Content
- Auto calculate mg of alcohol in a drink
- Widgets

---

## License

Same license as upstream. See `LICENSE`.

---

This fork exists for personal use and experimentation.

# ⚠️ Project Archived

This repository has been archived.
Active development continues in a private repository.

While there were a few external contributions, I’m grateful to everyone who contributed and have received their consent to relicense the project from GPL-3 to a proprietary license.
To make development sustainable, the app now includes a premium tier while keeping core features - including substance information and basic logging - free.
Because of this partial monetization, future versions are closed-source to prevent redistribution of paid functionality.

You can still explore this repository to learn from its earlier open-source implementation, but it will not receive further updates or support.

Copyright (C) 2022 Isaak Hanimann.

See the end of the file for license conditions.

# PsychonautWiki Journal

PsychonautWiki Journal is an Android app to make recreational drug users safer. The aim is to provide features that are attractive to users as well as useful from a harm-reduction perspective.
This app is built natively with [Jetpack Compose](https://developer.android.com/jetpack/compose).

![A presentation of the App](https://github.com/isaakhanimann/psychonautwiki-journal-android/blob/main/metadata/en-US/images/Google%20Pixel%204%20XL%20Presentation.png?raw=true)

<a href='https://play.google.com/store/apps/details?id=com.isaakhanimann.journal&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' height='100' /></a>
<a href='https://f-droid.org/en/packages/com.isaakhanimann.journal/'><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="100" /></a>

Or download the latest version from [GitHub releases](https://github.com/isaakhanimann/psychonautwiki-journal-android/releases/latest).

## License

    This file is part of PsychonautWiki Journal.
    
    PsychonautWiki Journal is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or (at
    your option) any later version.
    
    PsychonautWiki Journal is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with PsychonautWiki Journal.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
