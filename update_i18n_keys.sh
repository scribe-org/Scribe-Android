# Run this script to update the Scribe-i18n keys in the project.
# macOS: ./update_i18n_keys.sh
# Linux: ./update_i18n_keys.sh
# Windows: Run the commands below.
cd app/src/main/assets/i18n
git pull origin main
cd ../../../../..
