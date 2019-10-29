#!/usr/bin/python
# -*- coding: utf8 -*-

import sys
import codecs
import os

import csv
import json
from datetime import datetime


def convert_csv_to_json() :
	f = sys.stdin;
	reader = csv.reader(f);

	count_input = 0;
	for row in reader:

		count_input = count_input + 1;


		if count_input == 1 :
			header = row;
		else :

			print int(datetime.strptime( row[0], "%Y-%m" ).strftime("%s"));


if __name__ == '__main__':

	if len(sys.argv) == 1 :
		#print 'no argvs';
		convert_csv_to_json();

	elif len(sys.argv) == 2 :	
		print 'one argv', sys.argv[1];

	else :
		print 'argvs';

	
else:
	print 'I am being imported from another module'
