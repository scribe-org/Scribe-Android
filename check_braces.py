import re

s = open('app/src/keyboards/java/be/scri/services/GeneralKeyboardIME.kt', encoding='utf-8').read()
s = re.sub(r'//.*', '', s)
s = re.sub(r'/\*.*?\*/', '', s, flags=re.S)
s = re.sub(r'"([^"\\]|\\.)*"', '""', s)
s = re.sub(r"'([^'\\]|\\.)*'", "''", s)

opens = s.count('{')
closes = s.count('}')
print("Opens:", opens, "Closes:", closes, "Diff:", opens - closes)
