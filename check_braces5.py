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
    if count == 1:
        print(f"Back to 1 at line {line_num}")
    if line_num > 1800 and count > 1:
        pass # print(f"{line_num:4}: {count} {line.strip()[:40]}")
