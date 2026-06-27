import re

s = open('app/src/keyboards/java/be/scri/services/GeneralKeyboardIME.kt', encoding='utf-8').read()

def repl_block(m):
    return re.sub(r'[^\n]', ' ', m.group(0))

s = re.sub(r'/\*.*?\*/', repl_block, s, flags=re.S)
s = re.sub(r'//.*', '', s)
s = re.sub(r'"([^"\\]|\\.)*"', '""', s)
s = re.sub(r"'([^'\\]|\\.)*'", "''", s)

count = 0
for line_num, line in enumerate(s.split('\n'), 1):
    count += line.count('{')
    count -= line.count('}')
    print(f"{line_num:4}: {count} {line.strip()[:40]}")
