# Scribe color names

These names are the light and dark colors the Android app already paints. This file does not change a screen. Later pull requests will point Compose and the keyboard at these names. Old resource names stay in place, on the same hex, until a later change removes them.

Dark mode buttons stay orange on orange. `scribe_on_button` at night is `#FFD17B0F`, the same orange as the button. A Figma comparison of the few colors that differ is [#787](https://github.com/scribe-org/Scribe-Android/issues/787). This list does not adopt those Figma hexes.

`light_*` and `dark_*` pair names, `special_key_light`, `special_key_dark`, and `theme_scribe_blue` stay fixed hexes in both day and night. `theme_scribe_blue` stays `#FF4CADE6` in both. Do not point those names at a `scribe_*` token, because a night-qualified token would swap the color when the system night mode and the in-app theme disagree.

| Name | Light | Dark | Current resource |
| --- | --- | --- | --- |
| `scribe_background` | `#FF5EB5E8` | `#FF235270` | `light_scribe_color` / `dark_scribe_color`, and `background_color` |
| `scribe_text` | `#FF000000` | `#FFFFFFFF` | `light_text_color` / `dark_text_color`, and `text_color` |
| `scribe_surface` | `#FFFFFFFF` | `#FF000000` | card color in `Colors.kt` |
| `scribe_button` | `#FFFDAD0D` | `#FFD17B0F` | `light_tutorial_button_color`, `dark_button_outline_color` |
| `scribe_on_button` | `#FF000000` | `#FFD17B0F` | `button_text_color` |
| `scribe_selected` | `#FF235270` | `#FF5EB5E8` | selected button in `Colors.kt` |
| `scribe_outline` | `#FFFDAD0D` | `#FFD17B0F` | same orange as `scribe_button` |
| `scribe_success` | `#FF9BC53D` | `#FF08A045` | success color in `Colors.kt` |
| `scribe_caption` | `#E558564B` | `#FFCAC4D0` | light caption in `Colors.kt`. Dark is the grey the keyboard conjugation label already draws. Compose does not read it yet. |
| `scribe_switch_track` | `#FFAAAAAA` | `#FFAAAAAA` | switch container in `Colors.kt` |
| `scribe_switch_thumb` | `#FFFEDE9E` | `#FF2A1903` | switch selector in `Colors.kt` |
| `scribe_switch_thumb_off` | `#FFEEEEEE` | `#FF2D2D2D` | unchecked switch in `Colors.kt` |
| `scribe_corner_button` | `#FFFDAD0D` | `#FF2A1903` | corner button in `Colors.kt` |
| `scribe_keyboard_background` | `#FFD3D6DD` | `#FF1E1E1E` | `light_keyboard_bg_color` / `dark_keyboard_bg_color` |
| `scribe_key` | `#FFFFFFFF` | `#FF434343` | `light_key_color` / `dark_key_color` |
| `scribe_key_text` | `#95000000` | `#95FFFFFF` | `light_key_text_color` / `dark_key_text_color` |
| `scribe_key_pressed` | `#FFE9E9E9` | `#FF363636` | `light_key_press_color` / `dark_key_press_color` |
| `scribe_key_special` | `#FFAEB3BE` | `#FF202020` | `light_special_key_color` / `dark_special_key_color` |
| `scribe_accent_blue` | `#FF75CEFA` | `#FF4CADE6` | `light_scribe_blue` / `dark_scribe_blue`. Not `theme_scribe_blue`. |
| `scribe_annotation_red` | `#FF9F1722` | `#FFFB5F6C` | `annotateRed` |
| `scribe_annotation_blue` | `#FF335C99` | `#FF339EEB` | `annotateBlue` |
| `scribe_annotation_purple` | `#FF700589` | `#FFAC6DEC` | `annotatePurple` |
| `scribe_annotation_green` | `#FF3D7946` | `#FF85C26F` | `annotateGreen` |
| `scribe_annotation_orange` | `#FFF85A39` | `#FFFD9F5D` | `annotateOrange` |
| `scribe_nav_unselected` | `#FF808080` | `#FF808080` | `nav_item_grey` |
| `scribe_icon` | `#FF000000` | `#FFFFFFFF` | `icon_color` |

Spacing, corner shape, and elevation are not part of this list.
