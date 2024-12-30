LICENSE_TEXT="/*
 * Copyright (C) 2024 Scribe-Android
 *
 * This file is part of Scribe-Android and is licensed under the
 * GNU General Public License, version 3.
 * See LICENSE for more information.
 */"

for file in $(find . -name "*.kt"); do
    echo -e "$LICENSE_TEXT\n$(cat $file)" > $file
done
