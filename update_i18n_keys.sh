# Run this script to update the Scribe-i18n keys in the project.
# macOS: sh update_i18n_keys.sh
# Linux: bash update_i18n_keys.sh
# Windows: Run the command below.
git subtree pull --prefix app/src/main/assets/i18n git@github.com:scribe-org/Scribe-i18n.git main --squash
