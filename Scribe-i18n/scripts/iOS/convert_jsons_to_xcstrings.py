"""
Converts from Scribe-i18n localization JSON files to the Localizable.xcstrings file.


Usage:
    python3 Scribe-i18n/scripts/ios/convert_jsons_to_xcstrings.py
"""

import json
import os

directory = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
json_dir_list = os.listdir(os.path.join(directory, "jsons"))
languages = sorted(
    [file.replace(".json", "") for file in json_dir_list if file.endswith(".json")]
)
path = os.path.join(os.path.join(directory, "jsons"), "en-US.json")
file = open(path, "r").read()
file = json.loads(file)

data = {"sourceLanguage": "en"}
strings = {}
for key in file:
    language = {}

    for lang in languages:
        lang_json = json.loads(
            open(os.path.join(os.path.join(directory, "jsons"), f"{lang}.json"), "r").read()
        )

        translation = lang_json[key] if key in lang_json else ""
        if lang == "en-US":
            lang = "en"
        if translation != "":
            language[lang] = {"stringUnit": {"state": "", "value": translation}}

    strings[key] = {"comment": "", "localizations": language}

data |= {"strings": strings, "version": "1.0"}
file = open(os.path.join(directory, "Localizable.xcstrings"), "w")
json.dump(data, file, indent=2, ensure_ascii=False, separators=(",", " : "))

print(
    "Scribe-i18n localization JSON files successfully converted to the Localizable.xcstrings file."
)
