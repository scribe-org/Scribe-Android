"""
Converts from Scribe-i18n localization strings files to the JSON files.

Usage:
    python3 Scribe-i18n/Scripts/convert_strings_to_json.py
"""


import os
import json
import re

directory = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
dir_list = os.listdir(directory)
languages = sorted(
    [file.replace(".json", "") for file in dir_list if file.endswith(".json")]
)
regex = re.compile(r'<string name="(.*?)">(.*?)</string>', re.DOTALL)
values_directory = os.path.join(directory, 'values')
for lang in languages:
    path = os.path.join(values_directory, lang)
    with open(f'{path}/string.xml', 'r') as file:
        content = file.read()
    matches = regex.findall(content)
    result = dict(matches)
    with open(os.path.join(directory,f'{lang}.json'), 'w',encoding='utf-8') as file:
        json.dump(result, file, indent=4,ensure_ascii=False)

print(
    "Scribe-i18n localization strings files successfully converted to the JSON files."
)
