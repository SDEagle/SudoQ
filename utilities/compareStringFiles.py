#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys
import datetime
import re
from collections import defaultdict

englishLinesRaw = open(sys.argv[1]).readlines()
#fill a dictionary variable_name:string_value for english
only_english_strings =[l.strip() for l in filter(lambda x:'<string' in x, englishLinesRaw)]

engDic = {}
p_value = re.compile('>(.*)<')

for line in only_english_strings:
	key   = line.split('"')[1]
	value = p_value.search(line).group(1)
	engDic[key]=value


#delete all keys found in foreign
foreignLinesRaw = open(sys.argv[2]).readlines()
only_foreign_strings =[l.strip() for l in filter(lambda x:'<string' in x, foreignLinesRaw)]
for l in only_foreign_strings:
	key   = l.split('"')[1]
	if key not in engDic:
		print l
		#print engDic
		print key
	engDic.pop(key)

#unique keys remain
for k,v in engDic.iteritems():
	print k.ljust(60)+v