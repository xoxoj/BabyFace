#!/bin/bash

idx=1
for file in screenshot*.svg; do
	inkscape --export-area-page --export-png=screenshot$idx.png $file
	((idx++))
done
