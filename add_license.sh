#!/bin/bash

# Path to the license template
LICENSE_TEMPLATE="license_template.txt"

# Iterate through all .kt files
find . -name "*.kt" | while read -r FILE; do
    # Extract the file name and construct a description
    FILE_NAME=$(basename "$FILE")
    DESCRIPTION="This file contains the implementation for ${FILE_NAME%.kt}."

    # Create a temporary file with the updated license
    TEMP_FILE=$(mktemp)
    sed "s/{Description}/$DESCRIPTION/" "$LICENSE_TEMPLATE" > "$TEMP_FILE"

    # Add the license and the rest of the file
    cat "$TEMP_FILE" "$FILE" > "${FILE}.new"
    mv "${FILE}.new" "$FILE"

    # Clean up the temporary file
    rm "$TEMP_FILE"

    echo "Updated: $FILE"
done
