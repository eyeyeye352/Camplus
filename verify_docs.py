# -*- coding: utf-8 -*-
import zipfile
import re

files = [
    r'c:\Users\Lenovo\IdeaProjects\Camplus\Login用例规约.docx',
    r'c:\Users\Lenovo\IdeaProjects\Camplus\Login实现规约.docx',
]

for f in files:
    print(f'\n===== {f.split(chr(92))[-1]} =====')
    with zipfile.ZipFile(f) as z:
        doc = z.read('word/document.xml').decode('utf-8')

    paragraphs = doc.split('</w:p>')
    for p in paragraphs:
        texts = re.findall(r'<w:t[^>]*>([^<]*)</w:t>', p)
        if texts:
            line = ''.join(texts)
            if len(line.strip()) > 0:
                print(' *', line[:120])
