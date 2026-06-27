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
    if count == 2:
        print(f"Count is 2 at line {line_num}")
