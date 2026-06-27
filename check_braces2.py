import re

s = open('app/src/keyboards/java/be/scri/services/GeneralKeyboardIME.kt', encoding='utf-8').read()
s = re.sub(r'//.*', '', s)
s = re.sub(r'/\*.*?\*/', '', s, flags=re.S)
s = re.sub(r'"([^"\\]|\\.)*"', '""', s)
s = re.sub(r"'([^'\\]|\\.)*'", "''", s)

count = 0
for i, c in enumerate(s):
    if c == '{': count += 1
    elif c == '}':
        count -= 1
        if count < 0:
            print('Unbalanced at index', i)
            break
print('Final count:', count)
