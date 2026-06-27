import re

s = open('app/src/keyboards/java/be/scri/services/GeneralKeyboardIME.kt', encoding='utf-8').read()

# Replace block comments with spaces/newlines of the same length to preserve line numbers
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
    if count == 2:
        print(f"Count is 2 outside methods at line {line_num}: {line.strip()}")
