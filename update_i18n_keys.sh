# Run this script to update the Scribe-i18n keys in the project.
# macOS: sh update_i18n_keys.sh
# Linux: bash update_i18n_keys.sh
# Windows: Run the command below.
cd app/src/main/assets/i18n
git pull origin main
cd ../../..

git add app/src/main/assets/i18n
git commit -m "Update Scribe-i18n submodule"
