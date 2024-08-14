"""
Converts from Scribe-i18n localization JSON files to the Strings files.

Usage:
    python3 Scribe-i18n/Scripts/convert_jsons_to_strings.py
"""
import os
import json


def replace_special_characters(string):
    string = string.replace("'", "\\'")
    string = string.replace("&", "&amp;")
    string = string.replace("<", "&lt;")
    string = string.replace(">", "&gt;")
    return string

directory = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
dir_list = os.listdir(directory)
languages = sorted(
    [file.replace(".json", "") for file in dir_list if file.endswith(".json")]
)
values_directory = os.path.join(directory, 'values')
os.makedirs(values_directory, exist_ok=True)

for lang in languages:
    lang_dir = os.path.join(values_directory, lang)
    os.makedirs(lang_dir, exist_ok=True)

    xml_path = os.path.join(lang_dir,"string.xml")
    with open(xml_path, "w") as xml_file:
        xml_file.write('<?xml version="1.0" encoding="utf-8"?>\n')
        xml_file.write('<resources>\n')

        json_path = os.path.join(directory, f"{lang}.json")
        with open(json_path, "r") as json_file:
            json_data = json_file.read()
            json_data = json.loads(json_data)

            for key, value in json_data.items():
                value = replace_special_characters(value)
                xml_file.write(f'    <string name="{key}">{value}</string>\n')

        xml_file.write('</resources>\n')

print(
    "Scribe-i18n localization JSON files successfully converted to the strings file."
)
