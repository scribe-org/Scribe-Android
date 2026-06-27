import re

s = open('app/src/keyboards/java/be/scri/services/GeneralKeyboardIME.kt', encoding='utf-8').read()
s = re.sub(r'//.*', '', s)
s = re.sub(r'/\*.*?\*/', '', s, flags=re.S)
s = re.sub(r'"([^"\\]|\\.)*"', '""', s)
s = re.sub(r"'([^'\\]|\\.)*'", "''", s)

count = 0
for line_num, line in enumerate(s.split('\n'), 1):
    count += line.count('{')
    count -= line.count('}')
    if 1500 <= line_num <= 1650:
        print(f"{line_num:4}: {count} {line.strip()[:40]}")
